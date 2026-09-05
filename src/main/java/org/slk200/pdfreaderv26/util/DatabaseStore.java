package org.slk200.pdfreaderv26.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slk200.pdfreaderv26.bean.*;

import java.io.File;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 数据库操作工具类 (SQLite)
 * <p>
 * 负责管理数据库连接、表结构初始化以及各类业务数据的增删改查。
 * 数据库文件保存在 data/db_pdf_reader.db。
 * </p>
 */
public class DatabaseStore {

    private static final Logger LOGGER = Logger.getLogger(DatabaseStore.class.getName());

    // 数据库连接 URL
    private static final String DB_URL = "jdbc:sqlite:data/db_pdf_reader.db";

    // 单例连接对象
    private static Connection connection;

    // SQL 语句常量
    private static final String SQL_CREATE_HISTORY = "CREATE TABLE IF NOT EXISTS convert_history (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "time TEXT NOT NULL, " +
            "file_name TEXT NOT NULL, " +
            "file_path TEXT NOT NULL, " +
            "status TEXT NOT NULL)";

    private static final String SQL_CREATE_EXTRA = "CREATE TABLE IF NOT EXISTS extra_item (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT NOT NULL, " +
            "price TEXT NOT NULL)";

    private static final String SQL_CREATE_OPTION = "CREATE TABLE IF NOT EXISTS option_item (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "category TEXT NOT NULL, " +
            "name TEXT NOT NULL)";

    private static final String SQL_CREATE_ORDER_SEQ = "CREATE TABLE IF NOT EXISTS order_sequence (" +
            "seq_date TEXT NOT NULL PRIMARY KEY," +
            "current_val INTEGER NOT NULL DEFAULT 0," +
            "version INTEGER NOT NULL DEFAULT 0," +
            "update_time TEXT NOT NULL DEFAULT (datetime('now','localtime')))";

    private static final String SQL_CREATE_TICKET_NUM = "CREATE TABLE IF NOT EXISTS ticket_number(" +
            "t_id TEXT NOT NULL PRIMARY KEY," +
            "state TEXT NOT NULL DEFAULT '0'," +
            "update_time TEXT NOT NULL DEFAULT (datetime('now','localtime')))";

    private static final String SQL_CREATE_TICKET_CONTENT = "CREATE TABLE IF NOT EXISTS ticket_content(" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "t_id TEXT NOT NULL," +
            "file_type TEXT NOT NULL," +
            "file_name TEXT NOT NULL," +
            "state TEXT NOT NULL," +
            "page TEXT NOT NULL," +
            "file_path TEXT NOT NULL," +
            "note TEXT NOT NULL)";

    // 索引创建语句
    private static final String SQL_IDX_HISTORY_TIME = "CREATE INDEX IF NOT EXISTS idx_history_time ON convert_history(time)";
    private static final String SQL_IDX_OPTION_CAT = "CREATE INDEX IF NOT EXISTS idx_option_category ON option_item(category)";
    private static final String SQL_IDX_TICKET_CONTENT_TID = "CREATE INDEX IF NOT EXISTS idx_ticket_content_t_id ON ticket_content(t_id)";
    // 新增索引：优化工单列表查询
    private static final String SQL_IDX_TICKET_STATE_TIME = "CREATE INDEX IF NOT EXISTS idx_ticket_state_time ON ticket_number(state, update_time DESC)";

    // 清理旧数据 (90天)
    private static final String SQL_CLEAN_HISTORY = "DELETE FROM convert_history WHERE time < datetime('now', 'localtime', '-90 days')";

    private DatabaseStore() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() {
        return connection;
    }

    /**
     * 确保连接可用，若连接断开则重新建立并初始化表结构
     */
    private static synchronized void ensureConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                // SQLite 性能优化配置
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA synchronous=NORMAL");
                stmt.execute("PRAGMA busy_timeout=5000");
                stmt.execute("PRAGMA cache_size=-20000"); // 约20MB缓存
            }
            initTable();
        }
    }

    /**
     * 初始化数据库表结构及索引
     */
    private static void initTable() {
        try (Statement statement = connection.createStatement()) {
            // 创建表
            statement.executeUpdate(SQL_CREATE_HISTORY);
            statement.executeUpdate(SQL_CREATE_EXTRA);
            statement.executeUpdate(SQL_CREATE_OPTION);
            statement.executeUpdate(SQL_CREATE_ORDER_SEQ);
            statement.executeUpdate(SQL_CREATE_TICKET_NUM);
            statement.executeUpdate(SQL_CREATE_TICKET_CONTENT);

            // 创建索引
            statement.executeUpdate(SQL_IDX_HISTORY_TIME);
            statement.executeUpdate(SQL_IDX_OPTION_CAT);
            statement.executeUpdate(SQL_IDX_TICKET_CONTENT_TID);
            statement.executeUpdate(SQL_IDX_TICKET_STATE_TIME);

            // 清理过期历史记录
            statement.execute(SQL_CLEAN_HISTORY);

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "数据库表初始化失败", e);
        }
    }

    // ==========================================
    // 转换历史记录相关操作
    // ==========================================

    /**
     * 记录一条转换历史
     */
    public static synchronized void insert(String time, String fileName, String filePath, String status) {
        String sql = "INSERT INTO convert_history(time, file_name, file_path, status) VALUES (?,?,?,?)";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, time);
                statement.setString(2, fileName);
                statement.setString(3, filePath);
                statement.setString(4, status);
                statement.executeUpdate();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "写入转换历史失败", e);
        }
    }

    /**
     * 查询历史记录（重载方法，无日期筛选）
     */
    public static synchronized List<ConvertRecord> query(String keyword, String status) {
        return query(keyword, status, null, null);
    }

    /**
     * 查询历史记录，支持多条件筛选
     */
    public static synchronized List<ConvertRecord> query(String keyword, String status, String dateFrom, String dateTo) {
        List<ConvertRecord> records = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT time, file_name, file_path, status FROM convert_history WHERE 1=1");

        try {
            ensureConnection();
            boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
            boolean hasStatus = status != null && !status.trim().isEmpty() && !"全部".equals(status.trim());
            boolean hasFrom = dateFrom != null && !dateFrom.trim().isEmpty();
            boolean hasTo = dateTo != null && !dateTo.trim().isEmpty();

            if (hasKeyword) sql.append(" AND (file_name LIKE ? OR file_path LIKE ?)");
            if (hasStatus) sql.append(" AND status = ?");
            if (hasFrom) sql.append(" AND date(time) >= ?");
            if (hasTo) sql.append(" AND date(time) <= ?");
            sql.append(" ORDER BY time DESC");

            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                int index = 1;
                if (hasKeyword) {
                    String like = "%" + keyword.trim() + "%";
                    statement.setString(index++, like);
                    statement.setString(index++, like);
                }
                if (hasStatus) statement.setString(index++, status.trim());
                if (hasFrom) statement.setString(index++, dateFrom.trim());
                if (hasTo) statement.setString(index, dateTo.trim());

                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        records.add(new ConvertRecord(
                                resultSet.getString("time"),
                                resultSet.getString("file_name"),
                                resultSet.getString("file_path"),
                                resultSet.getString("status")));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "查询转换历史失败", e);
        }
        return records;
    }

    /**
     * 记录文件转换操作（自动判断成功/失败）
     */
    public static void recordConvert(String sourcePath) {
        File source = new File(sourcePath);
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        String pdfPath = source.getParent() + File.separator + (dot >= 0 ? name.substring(0, dot) : name) + ".pdf";
        String status = new File(pdfPath).exists() ? "成功" : "失败";
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        insert(time, name, sourcePath, status);
    }

    // ==========================================
    // 附加项与选项相关操作
    // ==========================================

    public static synchronized ObservableList<Extra> queryExtras() {
        ObservableList<Extra> extras = FXCollections.observableArrayList();
        String sql = "SELECT name, price FROM extra_item ORDER BY id";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    extras.add(new Extra(resultSet.getString("name"), resultSet.getString("price")));
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "查询附加项失败", e);
        }
        return extras;
    }

    /**
     * 全量替换附加项（使用事务和批处理）
     */
    public static synchronized void replaceExtras(List<Extra> extras) {
        String deleteSql = "DELETE FROM extra_item";
        String insertSql = "INSERT INTO extra_item(name, price) VALUES (?,?)";
        try {
            ensureConnection();
            connection.setAutoCommit(false);
            try (Statement delStmt = connection.createStatement();
                 PreparedStatement insStmt = connection.prepareStatement(insertSql)) {

                delStmt.executeUpdate(deleteSql);

                for (Extra extra : extras) {
                    insStmt.setString(1, extra.getName());
                    insStmt.setString(2, extra.getPrice());
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "保存附加项失败", e);
        }
    }

    public static synchronized List<String> queryOptions(String category) {
        List<String> names = new ArrayList<>();
        String sql = "SELECT name FROM option_item WHERE category = ? ORDER BY id";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, category);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        names.add(resultSet.getString("name"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "查询选项失败", e);
        }
        return names;
    }

    /**
     * 全量替换指定类别的选项（使用事务和批处理）
     */
    public static synchronized void replaceOptions(String category, List<String> names) {
        String deleteSql = "DELETE FROM option_item WHERE category = ?";
        String insertSql = "INSERT INTO option_item(category, name) VALUES (?,?)";
        try {
            ensureConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement delStmt = connection.prepareStatement(deleteSql);
                 PreparedStatement insStmt = connection.prepareStatement(insertSql)) {

                delStmt.setString(1, category);
                delStmt.executeUpdate();

                for (String name : names) {
                    insStmt.setString(1, category);
                    insStmt.setString(2, name);
                    insStmt.addBatch();
                }
                insStmt.executeBatch();
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "保存选项失败", e);
        }
    }

    // ==========================================
    // 工单相关操作
    // ==========================================

    /**
     * 提交工单（包含工单号和工单内容，使用事务）
     */
    public static void submitTicket(ObservableList<FileItem> fileItems, String Tid) {
        String insertTicketSql = "INSERT INTO ticket_number(t_id) VALUES(?)";
        String insertContentSql = "INSERT INTO ticket_content(t_id, file_type, file_name, state, page, file_path, note) VALUES (?,?,?,?,?,?,?)";

        try {
            ensureConnection();
            connection.setAutoCommit(false);
            try (PreparedStatement ticketStmt = connection.prepareStatement(insertTicketSql);
                 PreparedStatement contentStmt = connection.prepareStatement(insertContentSql)) {

                // 1. 插入工单号
                ticketStmt.setString(1, Tid);
                ticketStmt.executeUpdate();

                // 2. 批量插入工单内容
                for (FileItem fileItem : fileItems) {
                    contentStmt.setString(1, Tid);
                    contentStmt.setString(2, fileItem.getFile_type().toString());
                    contentStmt.setString(3, fileItem.getFile_name());
                    contentStmt.setString(4, fileItem.getFile_state().toString());
                    contentStmt.setString(5, fileItem.getFile_page());
                    contentStmt.setString(6, fileItem.getFile_path());
                    contentStmt.setString(7, fileItem.getFile_note());
                    contentStmt.addBatch();
                }
                contentStmt.executeBatch();

                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "提交工单失败", e);
        }
    }

    /**
     * 分页查询工单列表
     */
    public static ObservableList<TicketId> queryTicketIds(int offset) {
        ObservableList<TicketId> ticketIds = FXCollections.observableArrayList();
        String sql = "SELECT t_id, state, update_time FROM ticket_number ORDER BY update_time DESC, t_id DESC LIMIT 100 OFFSET ?";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, offset);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ticketIds.add(new TicketId(
                                resultSet.getString("t_id"),
                                resultSet.getString("state"),
                                resultSet.getString("update_time")));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "查询工单列表失败", e);
        }
        return ticketIds;
    }

    /**
     * 获取工单列表总页数
     */
    public static int queryTicketIdsPage() {
        String sql = "SELECT (COUNT(*) + 99) / 100 AS total_pages FROM ticket_number";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int pages = resultSet.getInt("total_pages");
                    return Math.max(pages, 1);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "计算工单列表页数失败", e);
        }
        return 1;
    }

    /**
     * 查询工单详细内容
     */
    public static ObservableList<TicketContent> queryTicketContent(String Tid) {
        ObservableList<TicketContent> ticketContents = FXCollections.observableArrayList();
        String sql = "SELECT file_name, state, page, file_path, note FROM ticket_content WHERE t_id = ?";
        try {
            ensureConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, Tid);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        ticketContents.add(new TicketContent(
                                resultSet.getString("file_name"),
                                resultSet.getString("state"),
                                resultSet.getString("page"),
                                resultSet.getString("file_path"),
                                resultSet.getString("note")));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "查询工单内容失败", e);
        }
        return ticketContents;
    }

    /**
     * 修改工单状态
     */
    public static void updateTicketState(String Tid) {
        String sql = "UPDATE ticket_number SET state = 1 WHERE t_id = ?";
        try {
            ensureConnection();
            // 注意：单条更新语句通常不需要手动事务控制，除非在复杂业务流中
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, Tid);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "修改工单状态失败", e);
        }
    }
}