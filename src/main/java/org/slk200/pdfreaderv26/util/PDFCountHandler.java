package org.slk200.pdfreaderv26.util;

import com.lowagie.text.pdf.PdfReader;
import org.slk200.pdfreaderv26.constant.FileType;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by tizzer on 2019/1/21.
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
     * @param file
     */
    public void count(File file) {
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(file.getAbsolutePath());
            page = String.valueOf(pdfReader.getNumberOfPages());
            totalPage += Integer.parseInt(page);
        } catch (IOException e) {
            Logger.getLogger(PDFCountHandler.class.getName()).log(Level.WARNING, "计算PDF页数错误", e);
        } finally {
            if (pdfReader != null) {
                pdfReader.close();
            }
        }
    }

    /**
     * 转换office为pdf
     *
     * @param inFile
     * @param fileType
     */
    public void office2pdf(String inFile, FileType fileType) {
        int lastIndex = inFile.lastIndexOf('.');
        String filenameWithoutSuffix = inFile.substring(0, lastIndex);
        String outFile = filenameWithoutSuffix + ".pdf";
        File TransedFile = new File(outFile);

        if (TransedFile.exists()) {
            page = "/";
            return;
        }

        if (!System.getProperty("os.name").toLowerCase().startsWith("win")) {
            page = "/";
            return;
        }
        //先判断是不是有重名文件文件存在了，否则不转换也不计数
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
        count(TransedFile);
    }
}
