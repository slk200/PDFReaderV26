module org.slk200.pdfreaderv26 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;
    requires java.prefs;
    requires io.github.osobolev.jacob;
    requires org.apache.pdfbox;

    opens org.slk200.pdfreaderv26 to javafx.fxml;
    opens org.slk200.pdfreaderv26.component to javafx.fxml;
    opens org.slk200.pdfreaderv26.controller to javafx.fxml;
    opens org.slk200.pdfreaderv26.dialog to javafx.fxml;
    exports org.slk200.pdfreaderv26;
    exports org.slk200.pdfreaderv26.controller;
    exports org.slk200.pdfreaderv26.bean;
    exports org.slk200.pdfreaderv26.constant;
    exports org.slk200.pdfreaderv26.util;
    exports org.slk200.pdfreaderv26.dialog;
}