package org.slk200.pdfreaderv26.controller;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slk200.pdfreaderv26.bean.TicketContent;
import org.slk200.pdfreaderv26.bean.TicketId;
import org.slk200.pdfreaderv26.cell.TextTableCell;
import org.slk200.pdfreaderv26.cell.TicketIdListCell;
import org.slk200.pdfreaderv26.util.DatabaseStore;

import java.util.Objects;

/**
 * 工单控制器，负责管理工单的内容展示、状态更新及翻页逻辑
 */
public class TicketController {

    // --- UI Components ---
    @FXML private Label fileLabel;
    @FXML private Label paperTypeLabel;
    @FXML private Label pageSizeLabel; // 修正了原代码中的拼写错误 pagerSize -> pageSize
    @FXML private Label singleOrDoubleLabel;
    @FXML private Label colorLabel;
    @FXML private Label pageRangeLabel;
    @FXML private Label printCopiesLabel;
    @FXML private Label packagingLabel;
    @FXML private Label postPressLabel;
    @FXML private Label noteLabel;
    @FXML private Button lastButton;
    @FXML private Button nextButton;
    @FXML private Label updateTimeLabel;
    @FXML private TableColumn<TicketContent, String> pageColumn;
    @FXML private TableColumn<TicketContent, String> noteColumn;
    @FXML private TableColumn<TicketContent, String> fileNameColumn;
    @FXML private Label ticketIdLabel;
    @FXML private TableView<TicketContent> ticketContentTable;
    @FXML private Button updateButton;
    @FXML private ListView<TicketId> ticketIdList;

    // --- Constants ---
    private static final int PAGE_SIZE = 100;
    private static final String STATE_COMPLETED = "1";

    // --- State Variables ---
    private int selectIndex = 0;
    private int offset = 0;
    private int totalPage = 1;
    private int nowPage = 1;

    /**
     * 初始化控制器，设置表格列属性、监听器及加载首屏数据
     */
    public void initController() {
        setupTableColumns();
        setupEventListeners();
        loadPageData();
    }

    /**
     * 配置表格列的属性和单元格工厂
     */
    private void setupTableColumns() {
        fileNameColumn.setCellValueFactory(new PropertyValueFactory<>("file_name"));
        fileNameColumn.setCellFactory(col -> new TextTableCell<>());

        pageColumn.setCellValueFactory(new PropertyValueFactory<>("page"));
        pageColumn.setCellFactory(col -> new TextTableCell<>(Pos.CENTER));

        noteColumn.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteColumn.setCellFactory(col -> new TextTableCell<>());

        // ListView 的 CellFactory 只需要设置一次
        ticketIdList.setCellFactory(param -> new TicketIdListCell());
    }

    /**
     * 设置 UI 组件的事件监听器
     */
    private void setupEventListeners() {
        // 工单列表点击事件
        ticketIdList.setOnMousePressed(event -> {
            int currentIndex = ticketIdList.getSelectionModel().getSelectedIndex();
            // 防止重复点击同一项触发刷新
            if (currentIndex != -1 && currentIndex != selectIndex) {
                selectIndex = currentIndex;
                TicketId selectedTicketId = ticketIdList.getItems().get(currentIndex);
                if (selectedTicketId != null) {
                    updateDetailView(selectedTicketId);
                }
            }
        });

        // 详情表格点击事件
        ticketContentTable.setOnMousePressed(event -> {
            TicketContent selectedContent = ticketContentTable.getSelectionModel().getSelectedItem();
            if (selectedContent != null) {
                fileLabel.setText(selectedContent.getFile_name());
                loadMark(selectedContent.getNote());
            }
        });
    }

    /**
     * 加载当前页码的数据
     */
    private void loadPageData() {
        ObservableList<TicketId> ticketIds = DatabaseStore.queryTicketIds(offset);

        if (!ticketIds.isEmpty()) {
            ticketIdList.getItems().setAll(ticketIds);

            // 只有在初始化或翻页时才重新计算总页数（假设 DatabaseStore 有对应方法，否则保持原逻辑）
            // 注意：原代码在 init 中调用 queryTicketIdsPage() 获取总页数，这里假设逻辑不变
            if (offset == 0) {
                totalPage = DatabaseStore.queryTicketIdsPage();
            }

            selectIndex = 0;
            ticketIdList.getSelectionModel().select(selectIndex);

            TicketId firstTicket = ticketIds.get(selectIndex);
            updateDetailView(firstTicket);
        } else {
            // 处理无数据情况
            resetDetailView();
            updateButton.setDisable(true);
        }

        updatePaginationButtons();
    }

    /**
     * 更新详情页 UI 显示
     */
    private void updateDetailView(TicketId ticketId) {
        ticketIdLabel.setText(ticketId.getTicketId());
        updateTimeLabel.setText(ticketId.getTicket_time());

        ticketContentTable.getItems().clear();
        ticketContentTable.getItems().addAll(DatabaseStore.queryTicketContent(ticketId.getTicketId()));

        updateButton.setDisable(Objects.equals(ticketId.getTicket_state(), STATE_COMPLETED));
        resetMark();
    }

    /**
     * 更新分页按钮状态
     */
    private void updatePaginationButtons() {
        lastButton.setDisable(nowPage == 1);
        nextButton.setDisable(nowPage == totalPage);
    }

    /**
     * 上一页
     */
    @FXML
    public void lastPage() {
        if (nowPage > 1) {
            offset -= PAGE_SIZE;
            nowPage--;
            loadPageData();
        }
    }

    /**
     * 下一页
     */
    @FXML
    public void nextPage() {
        if (nowPage < totalPage) {
            offset += PAGE_SIZE;
            nowPage++;
            loadPageData();
        }
    }

    /**
     * 更新工单状态为已完成
     */
    @FXML
    public void updateTicketState() {
        TicketId selectedTicketId = ticketIdList.getSelectionModel().getSelectedItem();
        if (selectedTicketId != null) {
            DatabaseStore.updateTicketState(selectedTicketId.getTicketId());
            selectedTicketId.setTicket_state(STATE_COMPLETED);
            ticketIdList.refresh();
            updateButton.setDisable(true);
        }
    }

    /**
     * 解析备注字符串并填充到对应的 Label 中
     */
    public void loadMark(String markText) {
        if (markText == null || markText.isEmpty()) {
            return;
        }
        // 使用全角分号分割
        String[] parts = markText.split("；");
        for (String part : parts) {
            String[] kv = part.split("：", 2);
            if (kv.length != 2) continue;

            String key = kv[0].trim();
            String value = kv[1].trim();

            switch (key) {
                case "纸张类型": paperTypeLabel.setText(value); break;
                case "纸张大小": pageSizeLabel.setText(value); break; // 修正变量引用
                case "单双面": singleOrDoubleLabel.setText(value); break;
                case "颜色": colorLabel.setText(value); break;
                case "页数范围": pageRangeLabel.setText(value); break;
                case "打印份数": printCopiesLabel.setText(value); break;
                case "封装工艺": packagingLabel.setText(value); break;
                case "印后工艺": postPressLabel.setText(value); break;
                case "备注": noteLabel.setText(value); break;
            }
        }
    }

    /**
     * 重置详情页 Label 为默认值
     */
    public void resetMark() {
        fileLabel.setText("文件");
        paperTypeLabel.setText("纸张类型");
        pageSizeLabel.setText("纸张大小");
        singleOrDoubleLabel.setText("单双面");
        colorLabel.setText("颜色");
        pageRangeLabel.setText("页数范围");
        printCopiesLabel.setText("打印份数");
        packagingLabel.setText("封装工艺");
        postPressLabel.setText("印后工艺");
        noteLabel.setText("备注");
    }

    /**
     * 重置详情页（当列表为空时调用）
     */
    private void resetDetailView() {
        ticketIdLabel.setText("");
        updateTimeLabel.setText("");
        ticketContentTable.getItems().clear();
        resetMark();
    }
}