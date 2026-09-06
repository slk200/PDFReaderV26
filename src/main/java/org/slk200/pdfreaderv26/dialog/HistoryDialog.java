package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.CustomCSS;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.HistoryController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

/**
 * 历史文件转换记录弹窗
 */
public class HistoryDialog extends Dialog<Void> {

    public HistoryDialog(Stage owner) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("history.fxml"));
        Parent content = fxmlLoader.load();

        HistoryController historyController = fxmlLoader.getController();
        historyController.initController();

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        this.setTitle("转换历史");
        this.initOwner(owner);
        ThemeManager.decorate(this.getDialogPane());
        this.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(HistoryDialog.class.getResource(CustomCSS.LOAD)).toExternalForm());
        this.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        this.getDialogPane().setContent(content);
        this.setResizable(true);
        this.show();
    }
}
