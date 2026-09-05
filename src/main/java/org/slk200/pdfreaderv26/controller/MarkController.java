package org.slk200.pdfreaderv26.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.dialog.InfoDialog;
import org.slk200.pdfreaderv26.factory.SafeIntegerSpinnerValueFactory;
import org.slk200.pdfreaderv26.manager.SafeSpinnerManager;
import org.slk200.pdfreaderv26.manager.SpinnerEditorManager;
import org.slk200.pdfreaderv26.util.DatabaseStore;

import java.util.List;

/**
 * 标记对话框控制器：收集打印设置并拼装成备注文本
 */
public class MarkController {

    // --- UI Components ---
    @FXML
    private ComboBox<String> paperTypeCombo;
    @FXML
    private ComboBox<String> paperSizeCombo;
    @FXML
    private RadioButton singleSideRadio;
    @FXML
    private RadioButton doubleSideRadio;
    @FXML
    private RadioButton bwRadio;
    @FXML
    private RadioButton colorRadio;
    @FXML
    private RadioButton allPagesRadio;
    @FXML
    private RadioButton customPagesRadio;
    @FXML
    private Spinner<Integer> rangeStartSpinner;
    @FXML
    private Spinner<Integer> rangeEndSpinner;
    @FXML
    private Spinner<Integer> copiesSpinner;
    @FXML
    private ComboBox<String> bindingCombo;
    @FXML
    private ComboBox<String> postPressCombo;
    @FXML
    private TextArea noteArea;

    private Stage owner;

    // --- Constants for Parsing (消除魔法值) ---
    private static final String KEY_PAPER_TYPE = "纸张类型";
    private static final String KEY_PAPER_SIZE = "纸张大小";
    private static final String KEY_SIDE = "单双面";
    private static final String KEY_COLOR = "颜色";
    private static final String KEY_RANGE = "页数范围";
    private static final String KEY_COPIES = "打印份数";
    private static final String KEY_BINDING = "封装工艺";
    private static final String KEY_POST_PRESS = "印后工艺";
    private static final String KEY_NOTE = "备注";

    // 数据库分类常量
    private static final String CATEGORY_PAPER_TYPE = "纸张类型";
    private static final String CATEGORY_PAPER_SIZE = "纸张大小";
    private static final String CATEGORY_BINDING = "封装工艺";
    private static final String CATEGORY_POST_PRESS = "印后工艺";

    public void setOwner(Stage owner) {
        this.owner = owner;
    }

    public void initController() {
        // 1. 初始化下拉框
        initCombo(paperTypeCombo, CATEGORY_PAPER_TYPE);
        initCombo(paperSizeCombo, CATEGORY_PAPER_SIZE);
        initCombo(bindingCombo, CATEGORY_BINDING);
        initCombo(postPressCombo, CATEGORY_POST_PRESS);

        // 2. 初始化单选按钮分组
        setupToggleGroup(singleSideRadio, doubleSideRadio);
        setupToggleGroup(bwRadio, colorRadio);
        setupToggleGroup(allPagesRadio, customPagesRadio);

        // 3. 初始化数值输入框 (Spinner)
        SafeIntegerSpinnerValueFactory factory = new SafeIntegerSpinnerValueFactory(1, Integer.MAX_VALUE, 1);
        rangeStartSpinner.setValueFactory(factory);
        rangeEndSpinner.setValueFactory(factory);
        copiesSpinner.setValueFactory(factory);

        // 应用安全修饰器
        SafeSpinnerManager.makeIntegerSafe(rangeStartSpinner, 1);
        SafeSpinnerManager.makeIntegerSafe(rangeEndSpinner, 1);
        SafeSpinnerManager.makeIntegerSafe(copiesSpinner, 1);
        SpinnerEditorManager.safeSpinner(rangeStartSpinner, 1);
        SpinnerEditorManager.safeSpinner(rangeEndSpinner, 1);
        SpinnerEditorManager.safeSpinner(copiesSpinner, 1);

        // 4. 绑定可用性逻辑：默认"所有页"，自定义范围不可用
        boolean isCustomRange = customPagesRadio.selectedProperty().get();
        rangeStartSpinner.setDisable(!isCustomRange);
        rangeEndSpinner.setDisable(!isCustomRange);

        // 监听选择变化以动态启用/禁用
        customPagesRadio.selectedProperty().addListener((_, _, isNowSelected) -> {
            boolean disable = !isNowSelected;
            rangeStartSpinner.setDisable(disable);
            rangeEndSpinner.setDisable(disable);
        });
    }

    /**
     * 辅助方法：简化 ToggleGroup 的创建
     */
    private void setupToggleGroup(RadioButton... radios) {
        ToggleGroup group = new ToggleGroup();
        for (RadioButton radio : radios) {
            radio.setToggleGroup(group);
        }
    }

    /**
     * 加载已有标记文本到表单控件
     *
     * @param markText 已有的标记文本
     */
    public void loadMark(String markText) {
        if (markText == null || markText.trim().isEmpty()) {
            return;
        }

        // 使用正则分割，防止中文分号或英文分号混用，并去除空项
        String[] parts = markText.split("；");

        for (String part : parts) {
            // 限制分割为2部分，防止值中包含冒号导致解析错误
            String[] kv = part.split("：", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case KEY_PAPER_TYPE:
                    setComboText(paperTypeCombo, value);
                    break;
                case KEY_PAPER_SIZE:
                    setComboText(paperSizeCombo, value);
                    break;
                case KEY_SIDE:
                    singleSideRadio.setSelected(!"双面".equals(value));
                    doubleSideRadio.setSelected("双面".equals(value));
                    break;
                case KEY_COLOR:
                    bwRadio.setSelected(!"彩色".equals(value));
                    colorRadio.setSelected("彩色".equals(value));
                    break;
                case KEY_RANGE:
                    handlePageRangeParsing(value);
                    break;
                case KEY_COPIES:
                    setSpinnerText(copiesSpinner, value.replace("份", ""));
                    break;
                case KEY_BINDING:
                    setComboText(bindingCombo, value);
                    break;
                case KEY_POST_PRESS:
                    setComboText(postPressCombo, value);
                    break;
                case KEY_NOTE:
                    noteArea.setText(value);
                    break;
                default:
                    // 可选：记录未知字段日志
                    break;
            }
        }
    }

    /**
     * 处理页数范围的特殊解析逻辑
     */
    private void handlePageRangeParsing(String value) {
        if ("所有页".equals(value)) {
            allPagesRadio.setSelected(true);
        } else {
            customPagesRadio.setSelected(true);
            // 提取数字，例如 "第1-5页" -> "1-5"
            String range = value.replace("第", "").replace("页", "");
            if (range.contains("-")) {
                String[] pages = range.split("-");
                if (pages.length == 2) {
                    setSpinnerText(rangeStartSpinner, pages[0]);
                    setSpinnerText(rangeEndSpinner, pages[1]);
                }
            } else {
                setSpinnerText(rangeStartSpinner, range);
                setSpinnerText(rangeEndSpinner, range);
            }
        }
    }

    /**
     * 安全设置 Spinner 文本
     */
    private void setSpinnerText(Spinner<Integer> spinner, String text) {
        if (text != null && !text.trim().isEmpty()) {
            spinner.getEditor().setText(text.trim());
        }
    }

    /**
     * 安全设置 ComboBox 文本
     */
    private void setComboText(ComboBox<String> combo, String text) {
        if (text != null) {
            combo.getEditor().setText(text);
        }
    }

    /**
     * 载入某个类别的选项
     */
    private void initCombo(ComboBox<String> combo, String category) {
        List<String> options = DatabaseStore.queryOptions(category);
        combo.getItems().addAll(options);
        // 限制下拉显示行数，避免遮挡
        combo.setVisibleRowCount(Math.clamp(options.size(), 3, 8));
        // 确保可编辑以支持自定义输入
        combo.setEditable(true);
    }

    /**
     * 确定前校验输入
     *
     * @return 校验通过返回true
     */
    public boolean validate() {
        if (customPagesRadio.isSelected()) {
            int start = parseSpinner(rangeStartSpinner);
            int end = parseSpinner(rangeEndSpinner);
            if (start < 1 || end < start) {
                new InfoDialog("页数范围不合法：起始页需不小于1，且结束页不能小于起始页！", owner);
                return false;
            }
        }
        if (parseSpinner(copiesSpinner) < 1) {
            new InfoDialog("打印份数需不小于1！", owner);
            return false;
        }
        return true;
    }

    /**
     * 把标记对话框中的设置拼装成一行备注文本
     */
    public String buildMarkText() {
        StringBuilder mark = new StringBuilder();

        appendPart(mark, KEY_PAPER_TYPE, textOf(paperTypeCombo));
        appendPart(mark, KEY_PAPER_SIZE, textOf(paperSizeCombo));
        appendPart(mark, KEY_SIDE, doubleSideRadio.isSelected() ? "双面" : "单面");
        appendPart(mark, KEY_COLOR, colorRadio.isSelected() ? "彩色" : "黑白");
        appendPart(mark, KEY_RANGE, buildPageRange());
        appendPart(mark, KEY_COPIES, parseSpinner(copiesSpinner) + "份");
        appendPart(mark, KEY_BINDING, textOf(bindingCombo));
        appendPart(mark, KEY_POST_PRESS, textOf(postPressCombo));

        // 备注字段通常放在最后，且允许为空字符串但需要处理null
        String noteText = noteArea.getText();
        if (noteText != null && !noteText.trim().isEmpty()) {
            appendPart(mark, KEY_NOTE, noteText.trim());
        }

        return mark.toString();
    }

    /**
     * 页数范围文本构建
     */
    private String buildPageRange() {
        if (customPagesRadio.isSelected()) {
            int start = parseSpinner(rangeStartSpinner);
            int end = parseSpinner(rangeEndSpinner);
            return start == end ? "第" + start + "页" : "第" + start + "-" + end + "页";
        }
        return "所有页";
    }

    /**
     * 取可编辑下拉框的文本（含手动输入内容）
     */
    private String textOf(ComboBox<String> combo) {
        String text = combo.getEditor().getText();
        return text == null ? "" : text.trim();
    }

    /**
     * 读取数字输入框的值，非法输入时回退到当前值
     */
    private int parseSpinner(Spinner<Integer> spinner) {
        try {
            String text = spinner.getEditor().getText().trim();
            return text.isEmpty() ? 0 : Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return spinner.getValue() == null ? 0 : spinner.getValue();
        }
    }

    /**
     * 追加一个非空的设置项
     */
    private void appendPart(StringBuilder mark, String label, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        if (!mark.isEmpty()) {
            mark.append("；");
        }
        mark.append(label).append("：").append(value);
    }
}