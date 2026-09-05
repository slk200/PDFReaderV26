package org.slk200.pdfreaderv26.controller;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 关于控制器
 */
public class AboutController {

    public void visit_Jacob() {
        visitWebPage("https://github.com/freemansoft/jacob-project");
    }

    public void visit_iText() {
        visitWebPage("https://itextpdf.com/");
    }

    public void visit_SQLLite() {
        visitWebPage("https://sqlite.org/index.html");
    }

    public void visit_github() {
        visitWebPage("https://github.com/slk200/PDFReader");
    }

    private void visitWebPage(String url) {
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            Logger.getLogger(AboutController.class.getName()).log(Level.WARNING, null, e);
        }
    }
}
