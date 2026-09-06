package org.slk200.pdfreaderv26.dialog;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import org.slk200.pdfreaderv26.constant.ImageSource;
import org.slk200.pdfreaderv26.constant.CustomCSS;
import org.slk200.pdfreaderv26.controller.TicketController;
import org.slk200.pdfreaderv26.manager.ThemeManager;

import java.io.IOException;
import java.util.Objects;

/**
 * 工单弹窗
 */
public class TicketDialog extends Dialog<Void> {

    public TicketDialog(Stage owner) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("ticket.fxml"));
        Parent content = fxmlLoader.load();

        TicketController ticketController = fxmlLoader.getController();
        ticketController.initController();

        Stage stage = (Stage) getDialogPane().getScene().getWindow();
        stage.getIcons().add(ImageSource.LOGO);

        this.setTitle("工单中心");
        this.getDialogPane().getStylesheets().add(Objects.requireNonNull(getClass().getResource(CustomCSS.LOAD)).toExternalForm());
        ThemeManager.decorate(this.getDialogPane());
        this.getDialogPane().setContent(content);
        this.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        this.setResizable(true);
        this.initOwner(owner);
        this.show();
    }

}
