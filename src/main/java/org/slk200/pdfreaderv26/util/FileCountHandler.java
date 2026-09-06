package org.slk200.pdfreaderv26.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slk200.pdfreaderv26.bean.FileItem;
import org.slk200.pdfreaderv26.constant.FileType;
import org.slk200.pdfreaderv26.constant.FileState;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 各类型文件计数处理器
 */
public class FileCountHandler {

    private static final Map<String, FileType> SUFFIX_MAP = new HashMap<>();

    // 初始化映射关系
    static {
        //PDF
        put("pdf", FileType.PDF);

        // Word
        put("doc", FileType.WORD);
        put("docx", FileType.WORD);
        put("wps", FileType.WORD);
        put("dot", FileType.WORD);
        put("wpt", FileType.WORD);

        // PPT
        put("ppt", FileType.PPT);
        put("pptx", FileType.PPT);
        put("dps", FileType.PPT);
        put("dpt", FileType.PPT);
        put("pot", FileType.PPT);
        put("pps", FileType.PPT);

        // Excel
        put("xls", FileType.EXCEL);
        put("xlsx", FileType.EXCEL);
        put("csv", FileType.EXCEL);
        put("xlt", FileType.EXCEL);
        put("et", FileType.EXCEL);
        put("ett", FileType.EXCEL);

        // Image
        put("jpg", FileType.IMAGE);
        put("jpeg", FileType.IMAGE);
        put("png", FileType.IMAGE);
        put("webp", FileType.IMAGE);
        put("bmp", FileType.IMAGE);
        put("gif", FileType.IMAGE);
        put("psd", FileType.IMAGE);
        put("psb", FileType.IMAGE);
        put("eps", FileType.IMAGE);
        put("ai", FileType.IMAGE);
        put("raw", FileType.IMAGE);
        put("tiff", FileType.IMAGE);
        put("ico", FileType.IMAGE);
        put("jpe", FileType.IMAGE);
        put("jfif", FileType.IMAGE);
        put("tif", FileType.IMAGE);
        put("svg", FileType.IMAGE);
        put("heic", FileType.IMAGE);
        put("heif", FileType.IMAGE);
    }

    private final ObservableList<FileItem> fileItems;
    // --- 计数器 ---
    private int wordNum;
    private int excelNum;
    private int pptNum;
    private int pdfNum;
    private int picNum;
    private int otherNum;

    public FileCountHandler() {
        fileItems = FXCollections.observableArrayList();
    }

    private static void put(String suffix, FileType type) {
        SUFFIX_MAP.put(suffix, type);
    }

    public int getWordNum() {
        return wordNum;
    }

    public int getExcelNum() {
        return excelNum;
    }

    public int getPptNum() {
        return pptNum;
    }

    public int getPdfNum() {
        return pdfNum;
    }

    public int getPicNum() {
        return picNum;
    }

    public int getOtherNum() {
        return otherNum;
    }

    public List<FileItem> getPendedFiles() {
        return fileItems;
    }

    /**
     * 1. 递归查找文件
     * 2. 根据后缀名映射表处理文件
     */
    public void process(File parent) {
        if (parent.isDirectory()) {
            File[] files = parent.listFiles();
            if (files != null) {
                for (File file : files) {
                    process(file);
                }
            }
        } else {
            String filenameWithSuffix = parent.getName().toLowerCase();

            // 过滤逻辑：非隐藏文件 或者 非Office临时文件 (~$开头)
            if (!parent.isHidden() || !filenameWithSuffix.startsWith("~$")) {

                int dotIndex = filenameWithSuffix.lastIndexOf(".");
                // 确保有点号且点号不在开头或结尾
                if (dotIndex > 0 && dotIndex < filenameWithSuffix.length() - 1) {
                    String suffix = filenameWithSuffix.substring(dotIndex + 1);

                    // 从 Map 中获取类型，如果不存在则为 null
                    FileType type = SUFFIX_MAP.get(suffix);

                    // 默认处理：Other
                    handleFile(parent, filenameWithSuffix, Objects.requireNonNullElse(type, FileType.OTHER));
                } else
                    //没有后缀名的文件
                    handleFile(parent, filenameWithSuffix, FileType.OTHER);
            }
        }
    }

    /**
     * 统一处理文件计数和列表添加
     */
    private void handleFile(File file, String filename, FileType type) {
        // 1. 计数
        switch (type) {
            case WORD:
                wordNum++;
                break;
            case EXCEL:
                excelNum++;
                break;
            case PPT:
                pptNum++;
                break;
            case PDF:
                pdfNum++;
                break;
            case IMAGE:
                picNum++;
                break;
            case OTHER:
                otherNum++;
                break;
        }

        FileState fileState;
        String page;

        if (type == FileType.PDF) {
            fileState = FileState.WITHOUT_CONVERT;
            page = "0";
        } else if (type == FileType.IMAGE || type == FileType.OTHER) {
            fileState = FileState.WITHOUT_CONVERT;
            page = "-";
        } else {
            fileState = FileState.FAILED;
            page = "0";
        }

        // 3. 添加到列表
        fileItems.add(new FileItem(type, filename, fileState, page, file.getAbsolutePath()));
    }
}