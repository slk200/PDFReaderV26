package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.util.Objects;

/**
 * Created by tizzer on 2019/01/26.
 */
public class AboutDialog extends Dialog<Void> {

    public AboutDialog(Stage owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("about.fxml"));
        Parent content = fxmlLoader.load();

        DialogPane dialogPane = getDialogPane();
        Stage stage = (Stage) dialogPane.getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);
        dialogPane.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/slk200/pdfreaderv26/css/custom.css")).toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");
        ThemeManager.decorate(dialogPane);
        dialogPane.setContent(content);
        dialogPane.getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
        this.setTitle("关于");
        this.initOwner(owner);
        this.showAndWait();
    }
}
