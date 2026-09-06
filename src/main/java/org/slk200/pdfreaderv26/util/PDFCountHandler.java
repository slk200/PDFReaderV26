package org.slk200.pdfreaderv26.util;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slk200.pdfreaderv26.constant.FileType;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * PDF文件计数处理器
 */
public class PDFCountHandler {

    private String page;
    private int totalPage;

    public String getPage() {
        return page;
    }

    public int getTotalPage() {
        return totalPage;
    }

    /**
     * 计算pdf总页数
     *
     * @param file 待计数的文件
     */
    public void parsePDF(File file) {
        try (PDDocument document = Loader.loadPDF(file)) {
            page = String.valueOf(document.getNumberOfPages());
            totalPage += Integer.parseInt(page);
        } catch (IOException e) {
            Logger.getLogger(PDFCountHandler.class.getName()).log(Level.WARNING, "计算PDF页数错误", e);
        }
    }

    /**
     * 转换office为pdf
     *
     * @param inFile   待转换的文件
     * @param fileType 待转换的文件类型
     */
    public void office2pdf(String inFile, FileType fileType) {
        //判断是什么平台，如果是MacOS/Linux平台，只统计PDF文件
        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            if (fileType == FileType.PDF) {
                parsePDF(new File(inFile));
            } else {
                page = "-";
            }
            return;
        }
        int lastIndex = inFile.lastIndexOf('.');
        String filenameWithoutSuffix = inFile.substring(0, lastIndex);
        String outFile = filenameWithoutSuffix + ".pdf";
        File TransedFile = new File(outFile);

        //先判断是不是有重名文件文件存在了，否则不转换也不计数
        if (TransedFile.exists()) {
            page = "-";
            return;
        }

        switch (fileType) {
            case WORD:
                CovertOfficeHandler.word2pdf(inFile, outFile);
                break;
            case PPT:
                CovertOfficeHandler.ppt2pdf(inFile, outFile);
                break;
            case EXCEL:
                CovertOfficeHandler.excel2pdf(inFile, outFile);
                break;
        }
        //转换的文件计入历史
        DatabaseStore.recordConvert(inFile);
        parsePDF(TransedFile);
    }
}
