package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.constant.CustomCSS;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.util.Objects;

/**
 * 关于弹窗
 */
public class AboutDialog extends Dialog<Void> {

    public AboutDialog(Stage owner) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("about.fxml"));
        Parent content = fxmlLoader.load();

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        this.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(CustomCSS.LOAD)).toExternalForm());
        ThemeManager.decorate(this.getDialogPane());
        this.getDialogPane().setContent(content);
        this.getDialogPane().getButtonTypes().add(new ButtonType("关闭", ButtonBar.ButtonData.CANCEL_CLOSE));
        this.setTitle("关于");
        this.initOwner(owner);
        this.show();
    }
}
