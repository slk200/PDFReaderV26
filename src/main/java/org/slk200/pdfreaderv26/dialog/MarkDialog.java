package org.slk200.pdfreaderv26.dialog;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.controller.MarkController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

public class MarkDialog extends Dialog<String> {

    public MarkDialog(Stage owner) throws IOException {
        this(owner, null);
    }

    public MarkDialog(Stage owner, String existingMark) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("mark.fxml"));
        Parent content = fxmlLoader.load();

        MarkController markController = fxmlLoader.getController();
        markController.setOwner(owner);
        markController.initController();
        if (existingMark != null && !existingMark.isEmpty()) {
            markController.loadMark(existingMark);
        }

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        setTitle("标记");
        initOwner(owner);
        ThemeManager.decorate(getDialogPane());
        getDialogPane().getStylesheets().add(
                Objects.requireNonNull(MarkDialog.class.getResource("/org/slk200/pdfreaderv26/css/custom.css")).toExternalForm());
        getDialogPane().setContent(content);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        setResizable(false);

        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!markController.validate()) {
                event.consume();
            }
        });

        setResultConverter(param -> param == ButtonType.OK ? markController.buildMarkText() : null);
    }
}