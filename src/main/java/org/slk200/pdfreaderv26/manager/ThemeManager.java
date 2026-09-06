package org.slk200.pdfreaderv26.manager;

import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import org.slk200.pdfreaderv26.constant.ThemeMode;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * 跨平台系统主题监听工具类（基于 JavaFX 22+ Platform.Preferences API）
 * <p>
 * 支持 Windows、macOS、Linux 三大平台，自动获取系统深色/浅色模式，
 * 并支持实时监听主题切换事件。
 * <p>
 * 使用示例：
 * <pre>
 *   // 初始化（在 JavaFX Application Thread 中调用）
 *   SystemThemeManager.init();
 *
 *   // 获取当前是否为暗黑模式
 *   boolean isDark = SystemThemeManager.isDarkMode();
 *
 *   // 监听主题变化
 *   SystemThemeManager.addListener(isDark -> {
 *       System.out.println("主题切换为: " + (isDark ? "暗黑" : "浅色"));
 *   });
 *
 *   // 自动将主题应用到 Scene
 *   SystemThemeManager.applyTheme(scene, "dark.css", "light.css");
 * </pre>
 */
public final class ThemeManager {

    private static final Logger LOGGER = Logger.getLogger(ThemeManager.class.getName());

    private static final ReadOnlyObjectWrapper<ColorScheme> colorSchemeProperty =
            new ReadOnlyObjectWrapper<>();

    private static final String DARK_STYLE_CLASS = "dark";
    private static final String PREF_KEY_THEME = "themeMode";

    private static ThemeMode currentMode = ThemeMode.LIGHT;
    private static Scene appScene;

    private static boolean initialized = false;

    public static void init(Scene scene) {
        appScene = scene;
        initSystemTheme();
        currentMode = loadMode();
        applyMode(currentMode);
    }

    /**
     * 初始化主题管理器，读取当前系统主题并注册监听器。
     * <p>
     * 必须在 JavaFX Application Thread 中调用（通常在 Application.start() 内）。
     * 重复调用不会重复注册。
     */
    private static void initSystemTheme() {
        if (initialized) {
            return;
        }

        Platform.Preferences preferences = Platform.getPreferences();

        ColorScheme initial = preferences.getColorScheme();
        colorSchemeProperty.set(initial);

        preferences.colorSchemeProperty().addListener((_, _, newScheme) -> {
            if (newScheme != null && !newScheme.equals(colorSchemeProperty.get())) {
                colorSchemeProperty.set(newScheme);
                notifyListeners(newScheme == ColorScheme.DARK);
            }
        });

        initialized = true;
    }

    /**
     * 获取当前是否为暗黑模式。
     * 调用前需先调用 {@link #initSystemTheme()}。
     *
     * @return true 表示暗黑模式，false 表示浅色模式
     */
    public static boolean isDarkMode() {
        ensureInitialized();
        return colorSchemeProperty.get() == ColorScheme.DARK;
    }

    /**
     * 获取当前系统主题方案。
     *
     * @return ColorScheme.LIGHT 或 ColorScheme.DARK
     */
    public static ColorScheme getColorScheme() {
        ensureInitialized();
        return colorSchemeProperty.get();
    }

    /**
     * 添加主题变化监听器。
     *
     * @param listener 回调函数，参数 isDark 表示切换后是否为暗黑模式
     */
    public static void addListener(ThemeChangeListener listener) {
        ensureInitialized();
        themeChangeListeners.add(listener);
    }

    /**
     * 移除主题变化监听器。
     *
     * @param listener 要移除的监听器
     */
    public static void removeListener(ThemeChangeListener listener) {
        themeChangeListeners.remove(listener);
    }

    /**
     * 为对话框装饰当前主题（在对话框显示前调用）
     *
     * @param dialogPane 对话框内容面板
     */
    public static void decorate(DialogPane dialogPane) {
        if (dialogPane == null) {
            return;
        }
        boolean dark = isDarkEffective(currentMode);
        boolean hasDark = dialogPane.getStyleClass().contains(DARK_STYLE_CLASS);
        if (dark && !hasDark) {
            dialogPane.getStyleClass().add(DARK_STYLE_CLASS);
        } else if (!dark && hasDark) {
            dialogPane.getStyleClass().remove(DARK_STYLE_CLASS);
        }
    }

    /**
     * 获取当前主题模式
     *
     * @return 当前主题模式
     */
    public static ThemeMode getCurrentMode() {
        return currentMode;
    }

    /**
     * 根据当前系统主题自动为 Scene 应用对应的 CSS 样式表。
     * <p>
     * 样式表路径为 classpath 资源路径，例如 "css/dark.css"。
     *
     */
    public static void applyMode(ThemeMode mode) {
        if (appScene == null) {
            return;
        }

        ensureInitialized();
        boolean dark = isDarkEffective(mode);
        boolean hasDark = appScene.getRoot().getStyleClass().contains(DARK_STYLE_CLASS);
        if (dark && !hasDark) {
            appScene.getRoot().getStyleClass().add(DARK_STYLE_CLASS);
        } else if (!dark && hasDark) {
            appScene.getRoot().getStyleClass().remove(DARK_STYLE_CLASS);
        }
    }

    /**
     * 设置主题模式并立即生效、持久化
     *
     * @param mode 目标主题模式
     */
    public static void setMode(ThemeMode mode) {
        currentMode = mode;
        saveMode(mode);
        applyMode(mode);
    }

    public static void setDarkMode(boolean dark) {
        if (dark) {
            applyMode(ThemeMode.DARK);
        } else {
            applyMode(ThemeMode.LIGHT);
        }
    }

    /**
     * 判断该模式下实际应使用的深浅色
     *
     * @param mode 主题模式
     * @return true表示深色
     */
    private static boolean isDarkEffective(ThemeMode mode) {
        if (mode == ThemeMode.DARK) {
            return true;
        }
        if (mode == ThemeMode.LIGHT) {
            return false;
        }
        return isSystemDark();
    }

    /**
     * 判断当前系统是否为深色主题
     *
     * @return 返回当前系统主题色
     */
    private static boolean isSystemDark() {
        return getColorScheme() == ColorScheme.DARK;
    }

    /**
     * 从持久化存储读取主题模式
     *
     * @return 主题模式，默认跟随系统
     */
    private static ThemeMode loadMode() {
        String name = Preferences.userNodeForPackage(ThemeManager.class)
                .get(PREF_KEY_THEME, ThemeMode.SYSTEM.name());
        try {
            return ThemeMode.valueOf(name);
        } catch (IllegalArgumentException e) {
            return ThemeMode.SYSTEM;
        }
    }

    /**
     * 持久化主题模式
     *
     * @param mode 主题模式
     */
    private static void saveMode(ThemeMode mode) {
        Preferences.userNodeForPackage(ThemeManager.class)
                .put(PREF_KEY_THEME, mode.name());
    }

    private static final List<ThemeChangeListener> themeChangeListeners =
            new CopyOnWriteArrayList<>();

    private static void ensureInitialized() {
        if (!initialized) {
            LOGGER.warning("SystemThemeManager 尚未初始化，请先调用 SystemThemeManager.init()");
        }
    }

    /**
     * 通知所有主题切换监听器
     * @param isDark 是否为深色主题
     */
    private static void notifyListeners(boolean isDark) {
        for (ThemeChangeListener listener : themeChangeListeners) {
            try {
                listener.onThemeChanged(isDark);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "通知所有主题切换监听器出错", e);
            }
        }
    }

    /**
     * 主题变化监听器函数式接口
     */
    @FunctionalInterface
    public interface ThemeChangeListener {
        /**
         * 当系统主题发生变化时回调。
         *
         * @param isDark true 表示切换为暗黑模式，false 表示切换为浅色模式
         */
        void onThemeChanged(boolean isDark);
    }
}