package org.slk200.pdfreaderv26.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * 一个线程安全的、用于持久化存储单个字符串的工具类。
 * 它在内存中缓存数据以实现快速访问，并自动同步到磁盘以实现持久化。
 */
public class SharedPreferences {

    private static final SharedPreferences INSTANCE = new SharedPreferences();

    private static final String KEY_STRING = "default_directory";
    private final Preferences prefs;
    private String cachedString;

    private SharedPreferences() {
        this.prefs = Preferences.userNodeForPackage(SharedPreferences.class);
        this.cachedString = prefs.get(KEY_STRING, "");
    }

    /**
     * 获取单例实例的全局访问点。
     *
     * @return StringStorage的唯一实例
     */
    public static SharedPreferences getInstance() {
        return INSTANCE;
    }

    /**
     * 获取持久化的字符串。
     * 该方法直接读取内存中的缓存，速度非常快。
     *
     * @return 存储的字符串
     */
    public String getString() {
        return cachedString;
    }

    /**
     * 设置并持久化一个字符串。
     * 该方法会同时更新内存缓存和磁盘存储。
     *
     * @param str 要存储的新字符串
     */
    public void setString(String str) {
        this.cachedString = str;
        // 将数据同步写入磁盘，确保下次启动时可用
        prefs.put(KEY_STRING, str);

        // 强制将更改从缓存刷新到持久化存储
        // 虽然put操作通常是异步的，但flush可以确保立即写入
        try {
            prefs.flush();
        } catch (Exception e) {
            // 在实际项目中，这里应该使用日志框架记录错误
            Logger.getLogger(SharedPreferences.class.getName()).log(Level.WARNING, "无法将字符串写入持久化存储: ", e);
        }
    }
}