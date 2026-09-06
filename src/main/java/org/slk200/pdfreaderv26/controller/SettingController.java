package org.slk200.pdfreaderv26.controller;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.bean.Extra;
import org.slk200.pdfreaderv26.factory.SafeDoubleSpinnerValueFactory;
import org.slk200.pdfreaderv26.manager.SafeSpinnerManager;
import org.slk200.pdfreaderv26.manager.SpinnerEditorManager;
import org.slk200.pdfreaderv26.util.DatabaseStore;
import org.slk200.pdfreaderv26.util.SharedPreferences;

import java.io.File;
import java.util.List;

/**
 * 系统设置控制器
 * 负责管理默认路径、附加费用及各类打印选项的配置
 */
public class SettingController {

    @FXML
    private TextField defaultPlace;
    @FXML
    private Label errorTip;
    @FXML
    private TableView<Extra> extraTable;
    @FXML
    private TableColumn<Extra, String> nameColumn;
    @FXML
    private TableColumn<Extra, String> priceColumn;
    @FXML
    private TextField nameField;
    @FXML
    private Spinner<Double> priceSpinner;
    @FXML
    private TableView<String> paperTypeTable;
    @FXML
    private TableColumn<String, String> paperTypeColumn;
    @FXML
    private TextField paperTypeField;
    @FXML
    private TableView<String> paperSizeTable;
    @FXML
    private TableColumn<String, String> paperSizeColumn;
    @FXML
    private TextField paperSizeField;
    @FXML
    private TableView<String> bindingTable;
    @FXML
    private TableColumn<String, String> bindingColumn;
    @FXML
    private TextField bindingField;
    @FXML
    private TableView<String> postPressTable;
    @FXML
    private TableColumn<String, String> postPressColumn;
    @FXML
    private TextField postPressField;

    private static final String CATEGORY_PAPER_TYPE = "纸张类型";
    private static final String CATEGORY_PAPER_SIZE = "纸张大小";
    private static final String CATEGORY_BINDING = "封装工艺";
    private static final String CATEGORY_POST_PRESS = "印后工艺";

    private boolean isDataModified = false;
    private boolean isOptionUpdated = false;
    private Stage stage;

    /**
     * 初始化控制器，绑定数据与事件
     */
    @FXML
    public void initController() {
        initExtraTable();
        initOptionTable(paperTypeTable, paperTypeColumn, CATEGORY_PAPER_TYPE);
        initOptionTable(paperSizeTable, paperSizeColumn, CATEGORY_PAPER_SIZE);
        initOptionTable(bindingTable, bindingColumn, CATEGORY_BINDING);
        initOptionTable(postPressTable, postPressColumn, CATEGORY_POST_PRESS);
        loadDefaultDirectory();
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    // --- Initialization Helpers ---

    private void initExtraTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // 初始化价格微调框
        priceSpinner.setValueFactory(new SafeDoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        SafeSpinnerManager.makeDoubleSafe(priceSpinner, 0);
        SpinnerEditorManager.safeSpinner(priceSpinner, 0.00);

        // 加载数据
        List<Extra> extras = DatabaseStore.queryExtras();
        extraTable.getItems().addAll(extras);
    }

    private void initOptionTable(TableView<String> table, TableColumn<String, String> column, String category) {
        column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue()));

        List<String> options = DatabaseStore.queryOptions(category);
        table.getItems().addAll(options);
    }

    private void loadDefaultDirectory() {
        String defaultDirectory = SharedPreferences.getInstance().getString();
        if (defaultDirectory != null && !defaultDirectory.isEmpty()) {
            File file = new File(defaultDirectory);
            if (file.exists()) {
                defaultPlace.setText(defaultDirectory);
            } else {
                errorTip.setText("警告：之前设置的地址当前已不存在！");
            }
        }
    }

    // --- Actions: Directory ---

    @FXML
    private void handleChooseDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择默认文件夹");

        // 尝试定位到当前路径
        File currentDir = new File(defaultPlace.getText());
        if (currentDir.exists()) {
            directoryChooser.setInitialDirectory(currentDir);
        }

        File selectedDirectory = directoryChooser.showDialog(stage);
        if (selectedDirectory != null) {
            SharedPreferences.getInstance().setString(selectedDirectory.getAbsolutePath());
            defaultPlace.setText(selectedDirectory.getAbsolutePath());
            errorTip.setText(null);
        }
    }

    // --- Actions: Extras ---

    @FXML
    private void handleAddExtra() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) return;

        String priceText = priceSpinner.getEditor().getText().trim();
        Extra extra = new Extra(name, priceText);

        extraTable.getItems().add(extra);
        nameField.clear();
        priceSpinner.getValueFactory().setValue(0.0);
        markDataModified();
    }

    @FXML
    private void handleDeleteExtra() {
        int selectedIndex = extraTable.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            extraTable.getItems().remove(selectedIndex);
            markDataModified();
        }
    }

    // --- Actions: Generic Options (Paper, Binding, etc.) ---

    @FXML
    private void handleAddPaperType() {
        addOption(paperTypeTable, paperTypeField);
    }

    @FXML
    private void handleDeletePaperType() {
        deleteOption(paperTypeTable);
    }

    @FXML
    private void handleAddPaperSize() {
        addOption(paperSizeTable, paperSizeField);
    }

    @FXML
    private void handleDeletePaperSize() {
        deleteOption(paperSizeTable);
    }

    @FXML
    private void handleAddBinding() {
        addOption(bindingTable, bindingField);
    }

    @FXML
    private void handleDeleteBinding() {
        deleteOption(bindingTable);
    }

    @FXML
    private void handleAddPostPress() {
        addOption(postPressTable, postPressField);
    }

    @FXML
    private void handleDeletePostPress() {
        deleteOption(postPressTable);
    }

    private void addOption(TableView<String> table, TextField field) {
        String value = field.getText().trim();
        if (value.isEmpty() || table.getItems().contains(value)) {
            return;
        }
        table.getItems().add(value);
        field.clear();
        markOptionUpdated();
    }

    private void deleteOption(TableView<String> table) {
        int selectedIndex = table.getSelectionModel().getSelectedIndex();
        if (selectedIndex >= 0) {
            table.getItems().remove(selectedIndex);
            markOptionUpdated();
        }
    }

    // --- State Management & Persistence ---

    /**
     * 标记基础数据（如附项）已修改
     */
    private void markDataModified() {
        this.isDataModified = true;
    }

    /**
     * 标记选项数据（如纸张类型）已修改
     */
    private void markOptionUpdated() {
        this.isOptionUpdated = true;
    }

    /**
     * 保存所有更改到数据库
     * 注意：此方法应仅在用户点击“保存”或关闭窗口确认保存时调用
     *
     * @return 是否有数据被保存
     */
    public boolean saveChanges() {
        boolean saved = false;

        if (isOptionUpdated) {
            DatabaseStore.replaceOptions(CATEGORY_PAPER_TYPE, paperTypeTable.getItems());
            DatabaseStore.replaceOptions(CATEGORY_PAPER_SIZE, paperSizeTable.getItems());
            DatabaseStore.replaceOptions(CATEGORY_BINDING, bindingTable.getItems());
            DatabaseStore.replaceOptions(CATEGORY_POST_PRESS, postPressTable.getItems());
            isOptionUpdated = false;
            saved = true;
        }

        if (isDataModified) {
            DatabaseStore.replaceExtras(extraTable.getItems());
            isDataModified = false;
            saved = true;
        }

        return saved;
    }

}