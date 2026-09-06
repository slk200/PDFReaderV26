package org.slk200.pdfreaderv26.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 基于 SQLite + 乐观锁的每日重置订单序号生成器
 * 线程安全：通过 synchronized(writeLock) 保证 SQLite 写串行化
 * （SQLite 本身不支持多写并发，乐观锁用于逻辑正确性校验）
 */
public class OrderSeqGenerator {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int MAX_RETRY = 5;
    private static final int SEQ_WIDTH = 6;

    private final Object writeLock = new Object();

    // ---- 预编译 SQL（在构造时准备，避免重复解析）----
    private final PreparedStatement psInsertIgnore;
    private final PreparedStatement psSelect;
    private final PreparedStatement psCasUpdate;

    // 监控指标
    private final AtomicLong totalGenerated = new AtomicLong(0);
    private final AtomicLong totalRetries = new AtomicLong(0);

    public OrderSeqGenerator(Connection connection) throws SQLException {
        // INSERT OR IGNORE：当天记录不存在则插入，已存在则忽略
        this.psInsertIgnore = connection.prepareStatement(
                "INSERT OR IGNORE INTO order_sequence(seq_date, current_val, version) VALUES (?, 0, 0)"
        );

        // 读取当前值和版本号
        this.psSelect = connection.prepareStatement(
                "SELECT current_val, version FROM order_sequence WHERE seq_date = ?"
        );

        // CAS 更新：同时校验 current_val 和 version
        this.psCasUpdate = connection.prepareStatement(
                "UPDATE order_sequence SET current_val = ?, version = version + 1 "
                        + "WHERE seq_date = ? AND current_val = ? AND version = ?"
        );
    }

    /**
     * 生成订单编号
     * 格式：ORD20260825000001
     *
     * @return 当日唯一递增订单号
     * @throws SeqExhaustedException 超过最大重试次数
     */
    public String generateOrderNo() {
        // ★ 只取一次时间，防止跨天边界不一致
        final LocalDate today = LocalDate.now();
        final String dateStr = today.format(DATE_FMT); // "2026-08-25"
        final String dateCompact = today.format(DateTimeFormatter.BASIC_ISO_DATE); // "20260825"

        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            try {
                long seq = doGenerate(today, dateStr);
                totalGenerated.incrementAndGet();

                return String.format("ORD%s%0" + SEQ_WIDTH + "d", dateCompact, seq);

            } catch (CasConflictException e) {
                totalRetries.incrementAndGet();
                if (attempt < MAX_RETRY) {
                    backoff(attempt);
                }
            }
        }

        throw new SeqExhaustedException(
                String.format("获取订单序号失败(date=%s)，已重试%d次", dateStr, MAX_RETRY));
    }

    /**
     * 单次 CAS 尝试
     * synchronized 保证 SQLite 写操作串行（SQLite 限制）
     */
    private long doGenerate(LocalDate today, String dateStr) {
        synchronized (writeLock) {
            try {
                // Step 1: 确保当天记录存在
                psInsertIgnore.setString(1, dateStr);
                psInsertIgnore.executeUpdate();

                // Step 2: 读取当前值 + 版本号
                psSelect.setString(1, dateStr);
                long oldVal;
                int oldVersion;
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        throw new CasConflictException("记录不存在: " + dateStr);
                    }
                    oldVal = rs.getLong("current_val");
                    oldVersion = rs.getInt("version");
                }

                long newVal = oldVal + 1;

                // Step 3: CAS 更新
                psCasUpdate.setLong(1, newVal);       // new current_val
                psCasUpdate.setString(2, dateStr);    // seq_date
                psCasUpdate.setLong(3, oldVal);       // expected old current_val
                psCasUpdate.setInt(4, oldVersion);    // expected old version

                int affected = psCasUpdate.executeUpdate();

                if (affected == 0) {
                    throw new CasConflictException(
                            String.format("CAS冲突: expected(val=%d,ver=%d)", oldVal, oldVersion));
                }

                return newVal;

            } catch (SQLException e) {
                throw new RuntimeException("数据库操作异常", e);
            }
        }
    }

    /**
     * 指数退避 + 随机抖动
     */
    private void backoff(int attempt) {
        try {
            long baseMs = (long) Math.pow(2, attempt - 1) * 5; // 5,10,20,40ms
            long jitter = ThreadLocalRandom.current().nextLong(0, baseMs / 2 + 1);
            Thread.sleep(baseMs + jitter);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ========== 内部异常 ==========

    private static class CasConflictException extends RuntimeException {
        CasConflictException(String msg) {
            super(msg);
        }
    }

    public static class SeqExhaustedException extends RuntimeException {
        SeqExhaustedException(String msg) {
            super(msg);
        }
    }
}