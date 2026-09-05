package org.slk200.pdfreaderv26.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.bean.Extra;
import org.slk200.pdfreaderv26.bean.ExtraItem;
import org.slk200.pdfreaderv26.bean.FileItem;
import org.slk200.pdfreaderv26.bean.SumRecord;
import org.slk200.pdfreaderv26.cell.FileTypeTableCell;
import org.slk200.pdfreaderv26.cell.StateTableCell;
import org.slk200.pdfreaderv26.cell.TextTableCell;
import org.slk200.pdfreaderv26.cell.UpDownTableCell;
import org.slk200.pdfreaderv26.component.BusyOverlay;
import org.slk200.pdfreaderv26.component.FloatingConvertButton;
import org.slk200.pdfreaderv26.constant.FileState;
import org.slk200.pdfreaderv26.constant.FileType;
import org.slk200.pdfreaderv26.constant.StringSource;
import org.slk200.pdfreaderv26.constant.ThemeMode;
import org.slk200.pdfreaderv26.dialog.*;
import org.slk200.pdfreaderv26.factory.SafeDoubleSpinnerValueFactory;
import org.slk200.pdfreaderv26.factory.SafeIntegerSpinnerValueFactory;
import org.slk200.pdfreaderv26.manager.SafeSpinnerManager;
import org.slk200.pdfreaderv26.manager.SpinnerEditorManager;
import org.slk200.pdfreaderv26.manager.ThemeManager;
import org.slk200.pdfreaderv26.util.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.slk200.pdfreaderv26.constant.ThemeMode.DARK;
import static org.slk200.pdfreaderv26.constant.ThemeMode.SYSTEM;

/**
 * 主控制器 - 负责协调UI交互、文件扫描、PDF计数与转换等核心业务流程。
 *
 * <p>优化说明（相对原始版本）：
 * <ul>
 *   <li>线程池由单线程改为固定大小线程池，适配IO密集型任务</li>
 *   <li>将 fileCountHandler / pdfCountHandler 从类成员变量移入 Task 内部，消除多线程竞争</li>
 *   <li>重命名易混淆的变量和方法，提高可读性</li>
 *   <li>异常处理细化为具体异常类型</li>
 *   <li>修复 openPDFFile 中文件名含多个点时的路径拼接风险</li>
 *   <li>synchronizedPages 增加数字格式前置校验</li>
 *   <li>线程池统一在 shutdownExecutor() 中关闭，确保资源释放原子性</li>
 * </ul>
 */
public class MainController {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    @FXML
    private BusyOverlay busyOverlay;
    @FXML
    private Button submitTicketButton;
    @FXML
    private MenuItem executeMenu;
    @FXML
    private Button executeButton;
    @FXML
    private TextField directoryField;
    @FXML
    private Label wordLabel;
    @FXML
    private Label pdfLabel;
    @FXML
    private Label excelLabel;
    @FXML
    private Label imageLabel;
    @FXML
    private Label pptLabel;
    @FXML
    private Label otherLabel;
    @FXML
    private Label totalAmountLabel;
    @FXML
    private Spinner<Integer> pageSpinner;
    @FXML
    private Spinner<Double> priceSpinner;
    @FXML
    private Spinner<Integer> numSpinner;
    @FXML
    private Spinner<Double> specSpinner;
    @FXML
    private ListView<SumRecord> sumList;
    @FXML
    private ComboBox<Extra> extraComboBox;
    @FXML
    private TableView<ExtraItem> extraTable;
    @FXML
    private TableColumn<ExtraItem, String> extraColumn;
    @FXML
    private TableColumn<ExtraItem, String> priceColumn;
    @FXML
    private TableColumn<ExtraItem, Integer> copiesColumn;
    @FXML
    private Label totalPage;
    @FXML
    private TableView<FileItem> fileItemTableView;
    @FXML
    private TableColumn<FileItem, FileType> typeColumn;
    @FXML
    private TableColumn<FileItem, String> nameColumn;
    @FXML
    private TableColumn<FileItem, FileState> stateColumn;
    @FXML
    private TableColumn<FileItem, String> pageColumn;
    @FXML
    private TableColumn<FileItem, String> pathColumn;
    @FXML
    private TableColumn<FileItem, String> noteColumn;
    @FXML
    private FloatingConvertButton floatingConvertButton;
    @FXML
    private RadioMenuItem systemThemeItem;
    @FXML
    private RadioMenuItem darkThemeItem;
    @FXML
    private RadioMenuItem lightThemeItem;
    @FXML
    private Menu themeMenu;
    @FXML
    private MenuItem openFileItem;
    @FXML
    private MenuItem openPDFFileItem;
    @FXML
    private MenuItem openFolderItem;
    @FXML
    private MenuItem markMenuItem;
    @FXML
    private MenuItem editMarkMenuItem;
    @FXML
    private MenuItem copyMarkMenuItem;
    @FXML
    private MenuItem pasteMarkMenuItem;

    // ==================== 内部状态 ====================

    /**
     * 使用固定大小线程池，核心数至少为2，适配IO密集型的文件转换与PDF计数任务。
     * 避免单线程下扫描和转换相互阻塞。
     */
    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors()));

    private final ObservableList<FileItem> items = FXCollections.observableArrayList();

    private String copiedMark;
    private Stage stage;
    private File selectedDirectory;

    /**
     * 当前累计总价（原变量名 sum 与局部变量冲突，重命名为 totalAmount）
     */
    private double totalAmount;

    // ==================== 初始化 ====================

    /**
     * 初始化控件绑定并首次加载数据。
     * 由 FXMLLoader 在加载完成后调用。
     */
    public void initController() {
        initPlaceholders();
        initFileTable();
        initSpinners();
        initExtraTable();
        initExtraComboBox();
        initConvertButton();
        initThemeMenu();
    }

    /**
     * 设置各列表/表格的占位提示文本。
     */
    private void initPlaceholders() {
        sumList.setPlaceholder(new Label("累计历史记录"));
        extraTable.setPlaceholder(new Label("附加项记录"));
        fileItemTableView.setPlaceholder(new Label("文件计数列表"));
    }

    /**
     * 初始化文件列表表格的列绑定与单元格工厂。
     */
    private void initFileTable() {
        fileItemTableView.setItems(items);
        fileItemTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileItemTableView.setOnMousePressed(event -> syncSelectedPageToSpinner());

        typeColumn.setCellValueFactory(new PropertyValueFactory<>("file_type"));
        typeColumn.setCellFactory(col -> new FileTypeTableCell<>());
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("file_name"));
        nameColumn.setCellFactory(col -> new TextTableCell<>());
        stateColumn.setCellValueFactory(new PropertyValueFactory<>("file_state"));
        stateColumn.setCellFactory(col -> new StateTableCell());
        pageColumn.setCellValueFactory(new PropertyValueFactory<>("file_page"));
        pageColumn.setCellFactory(col -> new TextTableCell<>(Pos.CENTER));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("file_path"));
        pathColumn.setCellFactory(col -> new TextTableCell<>());
        noteColumn.setCellValueFactory(new PropertyValueFactory<>("file_note"));
        noteColumn.setCellFactory(col -> new TextTableCell<>());
    }

    /**
     * 初始化四个数值Spinner控件的值工厂与安全防护。
     */
    private void initSpinners() {
        pageSpinner.setValueFactory(new SafeIntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        priceSpinner.setValueFactory(new SafeDoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));
        numSpinner.setValueFactory(new SafeIntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
        specSpinner.setValueFactory(new SafeDoubleSpinnerValueFactory(0, Double.MAX_VALUE, 0));

        // 防止 Spinner 在极端输入下抛出 NPE
        SafeSpinnerManager.makeIntegerSafe(pageSpinner, 0);
        SafeSpinnerManager.makeDoubleSafe(priceSpinner, 0);
        SafeSpinnerManager.makeIntegerSafe(numSpinner, 0);
        SafeSpinnerManager.makeDoubleSafe(specSpinner, 0);
        SpinnerEditorManager.safeSpinner(pageSpinner, 0);
        SpinnerEditorManager.safeSpinner(priceSpinner, 0.00);
        SpinnerEditorManager.safeSpinner(numSpinner, 0);
        SpinnerEditorManager.safeSpinner(specSpinner, 0.00);
    }

    /**
     * 初始化附加项表格的列绑定，包含份数增减联动总价逻辑。
     */
    private void initExtraTable() {
        extraColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        copiesColumn.setCellValueFactory(new PropertyValueFactory<>("num"));
        copiesColumn.setCellFactory(param -> new UpDownTableCell<>() {
            @Override
            public void incrementNext(int index) {
                super.incrementNext(index);
                ExtraItem extraItem = extraTable.getItems().get(index);
                totalAmount += Double.parseDouble(extraItem.getPrice());
                extraItem.setNum(extraItem.getNum() + 1);
                updateTotalPriceLabel();
            }

            @Override
            public void decrementNext(int index) {
                super.decrementNext(index);
                ExtraItem extraItem = extraTable.getItems().get(index);
                totalAmount -= Double.parseDouble(extraItem.getPrice());
                extraItem.setNum(extraItem.getNum() - 1);
                updateTotalPriceLabel();
            }
        });
    }

    /**
     * 初始化附加项下拉框数据。
     */
    private void initExtraComboBox() {
        extraComboBox.setVisibleRowCount(10);
        extraComboBox.setItems(DatabaseStore.queryExtras());
    }

    /**
     * 初始化悬浮转换按钮回调。
     */
    private void initConvertButton() {
        floatingConvertButton.setOnFilesReceived(this::convertFiles);
    }

    /**
     * 初始化主题切换菜单组。
     */
    private void initThemeMenu() {
        ToggleGroup themeGroup = new ToggleGroup();
        lightThemeItem.setToggleGroup(themeGroup);
        darkThemeItem.setToggleGroup(themeGroup);
        systemThemeItem.setToggleGroup(themeGroup);

        themeMenu.setOnShowing(event -> {
            ThemeMode currentMode = ThemeManager.getCurrentMode();
            if (currentMode == DARK) {
                darkThemeItem.setSelected(true);
            } else if (currentMode == SYSTEM) {
                systemThemeItem.setSelected(true);
            } else {
                lightThemeItem.setSelected(true);
            }
        });
    }

    // ==================== 舞台管理 ====================

    /**
     * 设置主舞台引用，并注册窗口关闭时的资源释放钩子。
     *
     * @param stage 软件主窗口
     */
    public void setStage(Stage stage) {
        this.stage = stage;
        stage.setOnCloseRequest(event -> shutdownExecutor());
    }

    /**
     * 统一关闭线程池，确保资源释放的原子性。
     * 在窗口关闭和退出操作中均调用此方法。
     */
    private void shutdownExecutor() {
        if (!executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    // ==================== 菜单功能区 ====================

    /**
     * 选择扫描文件夹。
     * <p>注意：FXML 中绑定的方法名为 scanFile，若重命名需同步修改 FXML 文件。</p>
     */
    public void scanDirectory() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("选择文件夹");

        String defaultPath = PrefStorage.getInstance().getString();
        File defaultDir = new File(defaultPath);
        if (defaultDir.exists()) {
            directoryChooser.setInitialDirectory(defaultDir);
        }

        File directory = directoryChooser.showDialog(stage);
        if (directory != null) {
            selectedDirectory = directory;
            directoryField.setText(selectedDirectory.getAbsolutePath());
        }
    }

    /**
     * 退出应用程序并释放线程池资源。
     */
    public void exit() {
        stage.close();
        shutdownExecutor();
    }

    /**
     * 打开设置对话框，设置关闭后刷新附加项下拉数据。
     */
    public void setting() {
        try {
            SettingDialog settingDialog = new SettingDialog(stage);
            boolean result = settingDialog.getResult();
            if (result) {
                extraComboBox.getItems().setAll(DatabaseStore.queryExtras());
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "打开设置对话框失败", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "设置对话框发生未知错误", e);
        }
    }

    /**
     * 执行文件扫描与PDF计数任务。
     *
     * <p>优化：fileCountHandler 和 pdfCountHandler 改为 Task 内局部变量，
     * 消除多线程环境下成员变量的状态竞争风险。任务完成后通过 onSucceeded 回调
     * 批量更新UI，避免在后台线程中直接操作UI控件。</p>
     */
    public void execute() {
        if (selectedDirectory == null) {
            return;
        }

        // 禁用可执行操作按钮
        disableButtons(true);
        // 重置控件数据状态（非清场模式）
        resetController(false);
        // 显示遮罩
        busyOverlay.show();

        Task<CountResult> countTask = new Task<>() {
            @Override
            protected CountResult call() {
                // 局部变量，避免多线程竞争
                FileCountHandler localFileCounter = new FileCountHandler();
                localFileCounter.process(selectedDirectory);

                // 在UI线程更新文件分类统计
                Platform.runLater(() -> updateStatistics(localFileCounter));

                List<FileItem> fileItems = localFileCounter.getPendedFiles();
                PDFCountHandler localPdfCounter = new PDFCountHandler();

                for (FileItem file : fileItems) {
                    busyOverlay.updateMessage("当前正在处理：" + file.getFile_path());
                    switch (file.getFile_type()) {
                        case EXCEL:
                        case PPT:
                            localPdfCounter.office2pdf(file.getFile_path(), file.getFile_type());
                            String page = localPdfCounter.getPage();
                            file.setFile_page(page);
                            // 如果路径里有 a.pdf 和 a.doc 同时存在，a.doc 不会执行转换，page 也显示 /
                            file.setFile_state("/".equals(page) ? FileState.WITHOUT_CONVERT : FileState.CONVERT_DONE);
                            break;
                        case PDF:
                            localPdfCounter.count(new File(file.getFile_path()));
                            file.setFile_page(localPdfCounter.getPage());
                            break;
                    }
                }
                return new CountResult(fileItems, localPdfCounter.getTotalPage());
            }
        };

        countTask.setOnSucceeded(_ -> {
            CountResult result = countTask.getValue();
            fileItemTableView.getItems().setAll(result.fileItems());
            typeColumn.setSortable(true);
            totalPage.setText("总页数：" + result.totalPage());
            disableButtons(false);
            busyOverlay.hide();
        });

        countTask.setOnFailed(_ -> {
            LOGGER.log(Level.WARNING, "文件计数任务执行失败", countTask.getException());
            disableButtons(false);
            busyOverlay.hide();
        });

        executorService.execute(countTask);
    }

    /**
     * 恢复/清场，将界面重置为初始状态。
     */
    public void reset() {
        resetController(true);
    }

    /**
     * 切换至浅色主题。
     */
    public void themeLight() {
        ThemeManager.setMode(ThemeMode.LIGHT);
    }

    /**
     * 切换至深色主题。
     */
    public void themeDark() {
        ThemeManager.setMode(ThemeMode.DARK);
    }

    /**
     * 切换为跟随系统主题。
     */
    public void themeSystem() {
        ThemeManager.setMode(ThemeMode.SYSTEM);
    }

    /**
     * 打开转换历史对话框。
     */
    public void history() {
        try {
            new HistoryDialog(stage).show();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "打开历史对话框失败", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "历史对话框发生未知错误", e);
        }
    }

    /**
     * 打开关于对话框。
     */
    public void about() {
        try {
            new AboutDialog(stage);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "打开关于对话框失败", e);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "关于对话框发生未知错误", e);
        }
    }

    // ==================== 单元格功能区 ====================

    /**
     * 同步选中行的页数到页数 Spinner。
     *
     * <p>优化：增加数字格式前置校验，避免依赖 catch 来处理正常业务流。</p>
     */
    private void syncSelectedPageToSpinner() {
        ObservableList<FileItem> selectedItems = fileItemTableView.getSelectionModel().getSelectedItems();
        if (selectedItems.size() != 1) {
            return;
        }
        FileItem fileItem = selectedItems.getFirst();
        String page = fileItem.getFile_page();
        // 前置校验：页码必须是有效数字才同步
        if (page != null && page.matches("\\d+")) {
            pageSpinner.getEditor().setText(page);
        }
    }

    // ==================== 按钮功能区 ====================

    /**
     * 提交工单：将当前文件列表提交至数据库。
     */
    public void ticketSubmit() {
        ObservableList<FileItem> fileItems = fileItemTableView.getItems();
        if (fileItems == null || fileItems.isEmpty()) {
            return;
        }
        try {
            OrderSeqGenerator orderSeqGenerator = new OrderSeqGenerator(DatabaseStore.getConnection());
            String ticketId = orderSeqGenerator.generateOrderNo();
            DatabaseStore.submitTicket(fileItems, ticketId);
            fileItemTableView.getItems().clear();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "提交工单失败", e);
        }
    }

    /**
     * 打开工单中心对话框。
     */
    public void openTicketCenter() {
        try {
            new TicketDialog(stage);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "打开工单中心失败", e);
        }
    }

    // ==================== 悬浮转换功能区 ====================

    /**
     * 执行文件转换任务。
     *
     * <p>将拖拽到悬浮按钮的文件列表进行 Office 到 PDF 的转换。
     * 转换在后台线程池中执行，完成后通过 Toast 通知用户。</p>
     *
     * @param files 鼠标拖拽到悬浮按钮的文件合集
     */
    private void convertFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        Map<File, FileType> convertible = new LinkedHashMap<>();
        Map<String, Integer> skippedBySuffix = new LinkedHashMap<>();

        for (File file : files) {
            collectConvertibles(file, convertible, skippedBySuffix);
        }

        // 提示不可转换的文件
        if (!skippedBySuffix.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : skippedBySuffix.entrySet()) {
                if (!sb.isEmpty()) {
                    sb.append('\n');
                }
                String fileName = entry.getKey();
                int count = entry.getValue();
                sb.append(count > 1
                        ? fileName + " 等 " + count + " 个"
                        : fileName);
                sb.append("：该格式文件无法转换");
            }
            floatingConvertButton.showToast(sb.toString());
        }

        if (convertible.isEmpty()) {
            return;
        }

        // 流光示意转换中
        floatingConvertButton.setConverting(true);
        Task<Void> convertTask = getConvertTask(convertible);

        executorService.execute(convertTask);
    }

    private Task<Void> getConvertTask(Map<File, FileType> convertible) {
        final int totalFiles = convertible.size();

        Task<Void> convertTask = new Task<>() {
            @Override
            protected Void call() {
                PDFCountHandler handler = new PDFCountHandler();
                for (Map.Entry<File, FileType> entry : convertible.entrySet()) {
                    handler.office2pdf(entry.getKey().getAbsolutePath(), entry.getValue());
                }
                return null;
            }
        };

        convertTask.setOnSucceeded(_ -> {
            floatingConvertButton.setConverting(false);
            floatingConvertButton.showToast("转换完成，共处理 " + totalFiles + " 个文件，结果保存在原目录");
        });

        convertTask.setOnFailed(_ -> {
            floatingConvertButton.setConverting(false);
            LOGGER.log(Level.WARNING, "文件转换任务失败", convertTask.getException());
            floatingConvertButton.showToast("转换过程出错，请重试");
        });
        return convertTask;
    }

    /**
     * 递归收集可转换的 Office 文件，不可转换的按文件名统计跳过次数。
     *
     * <p>优化：缓存 {@code file.getName().toLowerCase()} 到局部变量，
     * 避免循环中重复计算。</p>
     *
     * @param file        当前遍历的文件或目录
     * @param convertible 可转换文件收集容器
     * @param skipped     被跳过的文件按名称统计
     */
    private void collectConvertibles(File file, Map<File, FileType> convertible, Map<String, Integer> skipped) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    collectConvertibles(child, convertible, skipped);
                }
            }
            return;
        }

        String lowerName = file.getName().toLowerCase();
        if (file.isHidden() || lowerName.startsWith("~$")) {
            return;
        }

        int dot = lowerName.lastIndexOf('.');
        String suffix = (dot >= 0) ? lowerName.substring(dot + 1) : "";

        FileType type = resolveFileType(suffix);
        if (type != null) {
            convertible.put(file, type);
        } else {
            skipped.merge(file.getName(), 1, Integer::sum);
        }
    }

    /**
     * 根据文件后缀名解析对应的文件类型。
     *
     * @param suffix 小写文件后缀（不含点号）
     * @return 对应的 FileType，不支持的后缀返回 null
     */
    private FileType resolveFileType(String suffix) {
        return switch (suffix) {
            case "doc", "docx", "wps", "dot", "wpt" -> FileType.WORD;
            case "xls", "xlsx", "csv", "xlt", "et", "ett" -> FileType.EXCEL;
            case "ppt", "pptx", "dps", "dpt", "pot", "pps" -> FileType.PPT;
            default -> null;
        };
    }

    // ==================== 组件菜单功能区 ====================

    /**
     * 删除选中的累计记录，并同步更新总价。
     */
    public void deleteSum() {
        SumRecord sumRecord = sumList.getSelectionModel().getSelectedItem();
        if (sumRecord == null) {
            return;
        }
        double recordAmount = Double.parseDouble(sumRecord.getSum());
        totalAmount -= recordAmount;
        updateTotalPriceLabel();
        sumList.getItems().remove(sumRecord);
    }

    /**
     * 删除选中的附加项，并同步更新总价。
     */
    public void deleteExtra() {
        ExtraItem extraItem = extraTable.getSelectionModel().getSelectedItem();
        if (extraItem == null) {
            return;
        }
        totalAmount -= extraItem.getNum() * Double.parseDouble(extraItem.getPrice());
        updateTotalPriceLabel();
        extraTable.getItems().remove(extraItem);
    }

    /**
     * 打开选中行的原始文件。
     */
    public void openFile() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem != null) {
            openWithDesktop(new File(fileItem.getFile_path()), "右键菜单->打开文件");
        }
    }

    /**
     * 打开选中行对应的 PDF 文件。
     *
     * <p>优化：使用 {@link Path#resolveSibling(String)} 替换字符串截取，
     * 避免文件名包含多个点时路径拼接出错。</p>
     */
    public void openPDFFile() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem == null) {
            return;
        }

        File pdfFile;
        if (fileItem.getFile_type().equals(FileType.PDF)) {
            pdfFile = new File(fileItem.getFile_path());
        } else {
            // 使用 Path API 安全替换后缀，避免文件名含多个点时的截取风险
            Path originalPath = Paths.get(fileItem.getFile_path());
            String originalName = originalPath.getFileName().toString();
            int lastDot = originalName.lastIndexOf('.');
            String pdfName = (lastDot >= 0)
                    ? originalName.substring(0, lastDot) + ".pdf"
                    : originalName + ".pdf";
            pdfFile = originalPath.resolveSibling(pdfName).toFile();
        }

        if (pdfFile.exists()) {
            openWithDesktop(pdfFile, "右键菜单->打开PDF文件");
        }
    }

    /**
     * 在文件管理器中显示选中文件所在的文件夹。
     */
    public void showInExplore() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem != null) {
            File parentDir = new File(fileItem.getFile_path()).getParentFile();
            if (parentDir != null) {
                openWithDesktop(parentDir, "右键菜单->打开文件夹");
            }
        }
    }

    /**
     * 通用桌面打开操作，统一异常处理。
     *
     * @param file   要打开的文件或目录
     * @param action 操作描述，用于日志记录
     */
    private void openWithDesktop(File file, String action) {
        try {
            Desktop.getDesktop().open(file);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, action + " - 无法打开: " + file.getAbsolutePath(), e);
        }
    }

    // ==================== 状态管理 ====================

    /**
     * 重置/清场控制器状态。
     *
     * <p>当 isReset 为 true 时执行完整清场（包括目录选择、表格排序等），
     * 否则仅清理数据区域（列表、表格、价格等）。</p>
     *
     * @param isReset true=完整清场（恢复初始状态），false=仅清理数据
     */
    private void resetController(boolean isReset) {
        if (isReset) {
            selectedDirectory = null;
            directoryField.setText(null);
            fileItemTableView.getItems().clear();
            typeColumn.setSortable(false);
            updateStatistics(null);
        }
        sumList.getItems().clear();
        extraTable.getItems().clear();
        totalAmount = 0;
        copiedMark = null;
        updateTotalPriceLabel();
        totalPage.setText("总页数：0");
    }

    /**
     * 禁用/启用会改变文件列表数据的操作按钮。
     *
     * @param disabled true=禁用，false=启用
     */
    public void disableButtons(boolean disabled) {
        submitTicketButton.setDisable(disabled);
        executeMenu.setDisable(disabled);
        executeButton.setDisable(disabled);
    }

    /**
     * 更新文件分类统计标签。
     *
     * @param fileCountHandler 文件计数处理器，null 时重置所有标签
     */
    public void updateStatistics(FileCountHandler fileCountHandler) {
        if (fileCountHandler == null) {
            wordLabel.setText("Word 0");
            excelLabel.setText("EXCEL 0");
            pptLabel.setText("PPT 0");
            pdfLabel.setText("PDF 0");
            imageLabel.setText("图像 0");
            otherLabel.setText("其他 0");
            return;
        }
        wordLabel.setText("Word " + fileCountHandler.getWordNum());
        excelLabel.setText("EXCEL " + fileCountHandler.getExcelNum());
        pptLabel.setText("PPT " + fileCountHandler.getPptNum());
        pdfLabel.setText("PDF " + fileCountHandler.getPdfNum());
        imageLabel.setText("图像 " + fileCountHandler.getPicNum());
        otherLabel.setText("其他 " + fileCountHandler.getOtherNum());
    }

    /**
     * 统一更新总价标签文本，避免多处重复拼接。
     */
    private void updateTotalPriceLabel() {
        totalAmountLabel.setText(String.format(StringSource.TOTAL_PRICE + StringSource.FORMAT + StringSource.UNIT, totalAmount));
    }

    // ==================== 右键菜单功能区 ====================

    /**
     * 待处理表格右键菜单显示前，根据选中行数动态启用/禁用菜单项。
     */
    public void onPendedMenuShowing() {
        int selectedCount = fileItemTableView.getSelectionModel().getSelectedItems().size();
        boolean singleSelected = selectedCount == 1;

        openFileItem.setDisable(!singleSelected);
        openPDFFileItem.setDisable(!singleSelected);
        openFolderItem.setDisable(!singleSelected);
        markMenuItem.setDisable(!singleSelected);
        editMarkMenuItem.setDisable(!singleSelected);
        copyMarkMenuItem.setDisable(!singleSelected);
        pasteMarkMenuItem.setDisable(selectedCount == 0 || copiedMark == null);
    }

    /**
     * 标记：打开标记对话框，将备注写入选中行。
     */
    public void mark() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem == null) {
            return;
        }
        try {
            MarkDialog markDialog = new MarkDialog(stage);
            Optional<String> result = markDialog.showAndWait();
            result.ifPresent(fileItem::setFile_note);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "右键菜单->标记 失败", e);
        }
    }

    /**
     * 修改标记：打开标记对话框并预填当前备注内容。
     */
    public void editMark() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem == null) {
            return;
        }
        try {
            MarkDialog markDialog = new MarkDialog(stage, fileItem.getFile_note());
            Optional<String> result = markDialog.showAndWait();
            result.ifPresent(fileItem::setFile_note);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "右键菜单->修改标记 失败", e);
        }
    }

    /**
     * 复制标记：将选中行的备注保存到临时变量。
     */
    public void copyMark() {
        FileItem fileItem = fileItemTableView.getSelectionModel().getSelectedItem();
        if (fileItem == null) {
            return;
        }
        String note = fileItem.getFile_note();
        copiedMark = (note != null && !note.isEmpty()) ? note : null;
    }

    /**
     * 粘贴标记：将临时标记内容写入所有选中行的备注列。
     */
    public void pasteMark() {
        if (copiedMark == null) {
            return;
        }
        ObservableList<FileItem> items = fileItemTableView.getSelectionModel().getSelectedItems();
        for (FileItem item : items) {
            item.setFile_note(copiedMark);
        }
    }

    // ==================== 价格计算功能区 ====================

    /**
     * 累计价格：根据页数、单价、份数和附加费计算并累加到总价。
     *
     * <p>计算公式：subtotal = page * price * num + spec，总价 += subtotal</p>
     *
     * <p>注意：原方法名为 sum()，与成员变量名冲突易混淆，
     * 建议 FXML 同步更名为 addToTotal。</p>
     */
    public void sum() {
        int page = Integer.parseInt(pageSpinner.getEditor().getText());
        double price = Double.parseDouble(priceSpinner.getEditor().getText());
        int num = Integer.parseInt(numSpinner.getEditor().getText());
        double spec = Double.parseDouble(specSpinner.getEditor().getText());

        double subtotal = page * price * num + spec;
        totalAmount += subtotal;

        updateTotalPriceLabel();
        SumRecord sumRecord = new SumRecord(page, price, num, spec, String.format(StringSource.FORMAT, subtotal));
        sumList.getItems().addFirst(sumRecord);
    }

    /**
     * 添加附加项：从下拉框选择预设项，添加到附加项表格并累加到总价。
     */
    public void addExtra() {
        Extra selectedItem = extraComboBox.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        extraTable.getItems().addFirst(new ExtraItem(selectedItem.getName(), selectedItem.getPrice(), 1));
        totalAmount += Double.parseDouble(selectedItem.getPrice());
        updateTotalPriceLabel();
    }

    // ==================== 内部数据载体 ====================

    /**
     * 文件计数任务的返回结果封装，避免通过成员变量传递中间状态。
     *
     * @param fileItems 处理后的文件列表
     * @param totalPage 总页数
     */
    private record CountResult(List<FileItem> fileItems, int totalPage) {
    }
}
