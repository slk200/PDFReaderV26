package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.HistoryController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

public class HistoryDialog extends Dialog<Void> {

    public HistoryDialog(Stage owner) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("history.fxml"));
        Parent content = fxmlLoader.load();

        HistoryController historyController = fxmlLoader.getController();
        historyController.initController();

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        setTitle("转换历史");
        initOwner(owner);
        ThemeManager.decorate(getDialogPane());
        getDialogPane().getStylesheets().add(
                Objects.requireNonNull(HistoryDialog.class.getResource("/org/slk200/pdfreaderv26/css/custom.css")).toExternalForm());
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setContent(content);
        setResizable(true);
    }
}
