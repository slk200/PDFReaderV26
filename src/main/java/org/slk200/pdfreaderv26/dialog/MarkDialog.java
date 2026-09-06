package org.slk200.pdfreaderv26.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.bean.FileItem;
import org.slk200.pdfreaderv26.constant.CustomCSS;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.MarkController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

/**
 * 标记弹窗
 */
public class MarkDialog extends Dialog<String> {

    public MarkDialog(Stage owner, FileItem fileItem) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mark.fxml"));
        Parent content = fxmlLoader.load();

        MarkController markController = fxmlLoader.getController();
        markController.initController(Integer.parseInt(fileItem.getFile_page()));
        markController.loadMark(fileItem.getFile_note());

        Stage stage = (Stage) this.getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        this.setTitle("标记");
        this.initOwner(owner);
        ThemeManager.decorate(this.getDialogPane());
        this.getDialogPane().getStylesheets().add(
                Objects.requireNonNull(MarkDialog.class.getResource(CustomCSS.LOAD)).toExternalForm());
        this.getDialogPane().setContent(content);
        this.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        this.setResizable(false);

        Button okButton = (Button) this.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!markController.validate()) {
                event.consume();
            }
        });

        setResultConverter(param -> param == ButtonType.OK ? markController.buildMarkText() : null);
    }
}