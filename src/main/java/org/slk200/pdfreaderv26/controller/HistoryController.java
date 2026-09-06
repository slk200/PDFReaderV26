package org.slk200.pdfreaderv26.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slk200.pdfreaderv26.bean.ConvertRecord;
import org.slk200.pdfreaderv26.cell.TextTableCell;
import org.slk200.pdfreaderv26.util.DatabaseStore;

/**
 * 转换历史记录控制器
 *
 * <p>以表格展示每次转换的时间、文件名、路径与状态，
 * 支持按状态筛选、按文件名或路径关键词搜索、按日期范围过滤。</p>
 */
public class HistoryController {
    @FXML
    private Button lastButton;
    @FXML
    private Button nextButton;
    @FXML
    private TextField keywordField;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private Button resetButton;
    @FXML
    private Label recordsLabel;
    @FXML
    private DatePicker fromPicker;
    @FXML
    private DatePicker toPicker;
    @FXML
    private TableView<ConvertRecord> convertRecordTableView;
    @FXML
    private TableColumn<ConvertRecord, String> timeColumn;
    @FXML
    private TableColumn<ConvertRecord, String> nameColumn;
    @FXML
    private TableColumn<ConvertRecord, String> pathColumn;
    @FXML
    private TableColumn<ConvertRecord, String> statusColumn;

    private ObservableList<ConvertRecord> tempItems;

    private int TOTAL_RECORDS = 0;
    private int NOW_PAGE = 1;
    private int TOTAL_PAGE = 1;
    private int OFFSET = 0;

    /**
     * 初始化控件绑定并首次加载数据
     */
    public void initController() {
        //状态下拉项
        statusBox.getItems().addAll("全部", "成功", "失败");
        statusBox.setValue("全部");
        //列绑定
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("convert_time"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("file_name"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("file_path"));
        pathColumn.setCellFactory(_ -> new TextTableCell<>());
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("file_status"));
        //占位提示
        convertRecordTableView.setPlaceholder(new Label("暂无转换记录"));

        //筛选与搜索实时生效
        keywordField.textProperty().addListener((_, _, _) -> loadRecords());
        statusBox.valueProperty().addListener((_, _, _) -> loadRecords());
        fromPicker.valueProperty().addListener((_, _, _) -> loadRecords());
        toPicker.valueProperty().addListener((_, _, _) -> loadRecords());
        resetButton.setOnAction(_ -> {
            keywordField.clear();
            statusBox.setValue("全部");
            fromPicker.setValue(null);
            toPicker.setValue(null);
            loadRecords();
        });
        loadRecords();
    }

    /**
     * 按当前条件筛选并加载数据
     */
    private void loadRecords() {
        String from = fromPicker.getValue() == null ? null : fromPicker.getValue().toString();
        String to = toPicker.getValue() == null ? null : toPicker.getValue().toString();
        ObservableList<ConvertRecord> items = FXCollections.observableArrayList(
                DatabaseStore.query(keywordField.getText(), statusBox.getValue(), from, to));
        NOW_PAGE = 1;
        TOTAL_PAGE = 1;
        OFFSET = 0;
        tempItems = FXCollections.observableArrayList(items);
        TOTAL_RECORDS = items.size();
        if (TOTAL_RECORDS > 100) {
            items.remove(100, TOTAL_RECORDS);
            TOTAL_PAGE = (int) Math.ceil(TOTAL_RECORDS / 100.0);
        }
        convertRecordTableView.getItems().setAll(items);
        recordsLabel.setText("共 " + TOTAL_RECORDS + " 条记录");
        lastButton.setDisable(NOW_PAGE == 1);
        nextButton.setDisable(NOW_PAGE == TOTAL_PAGE);
    }

    /**
     * 上一页
     */
    public void lastPage() {
        OFFSET = OFFSET - 100;
        ObservableList<ConvertRecord> items = FXCollections.observableArrayList(tempItems);
        if (!items.isEmpty()) {
            --NOW_PAGE;
            lastButton.setDisable(NOW_PAGE == 1);
            nextButton.setDisable(NOW_PAGE == TOTAL_PAGE);
            items.remove(OFFSET + 100, TOTAL_RECORDS);
            if (NOW_PAGE != 1) {
                items.remove(0, OFFSET);
            }
            convertRecordTableView.getItems().setAll(items);
        }
    }

    /**
     * 下一页
     */
    public void nextPage() {
        OFFSET = OFFSET + 100;
        ObservableList<ConvertRecord> items = FXCollections.observableArrayList(tempItems);
        if (!items.isEmpty()) {
            ++NOW_PAGE;
            lastButton.setDisable(NOW_PAGE == 1);
            nextButton.setDisable(NOW_PAGE == TOTAL_PAGE);
            items.remove(0, OFFSET);
            if (NOW_PAGE != TOTAL_PAGE) {
                items.remove(100, TOTAL_RECORDS - OFFSET);
            }
            convertRecordTableView.getItems().setAll(items);
        }
    }
}
