package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.SettingController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

/**
 * Created by tizzer on 2019/1/21.
 */
public class SettingDialog extends Dialog<Boolean> {

    public SettingDialog(Stage owner) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("setting.fxml"));
        Parent content = fxmlLoader.load();

        SettingController settingController = fxmlLoader.getController();
        settingController.initController();

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);
        settingController.setStage(stage);

        this.setTitle("设置");
        this.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource("/org/slk200/pdfreaderv26/css/custom.css")).toExternalForm());
        ThemeManager.decorate(this.getDialogPane());
        this.getDialogPane().setContent(content);
        this.setResizable(true);
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.setResultConverter(param -> {
            if (param == ButtonType.OK) {
                return settingController.saveChanges();
            } else {
                return false;
            }
        });
        this.initOwner(owner);
        this.showAndWait();
    }
}
