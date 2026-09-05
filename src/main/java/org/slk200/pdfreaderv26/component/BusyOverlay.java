package org.slk200.pdfreaderv26.component;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * busy_pane，在有文件计数时显示
 */
public class BusyOverlay extends StackPane {

    private final Label messageLabel;

    public BusyOverlay() {
        VBox contentBox = new VBox(16);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setStyle("""
                    -fx-background-color: rgba(128,128,128,0.5);
                    -fx-background-radius: 18;
                """);

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS); // -1 = indeterminate
        progressIndicator.setMaxSize(60, 60);

        messageLabel = new Label();
        messageLabel.setMaxWidth(400);
        messageLabel.setTextOverrun(OverrunStyle.CENTER_WORD_ELLIPSIS);
        messageLabel.setStyle("""
                    -fx-font-size: 15px;
                    -fx-text-fill: #333333;
                    -fx-font-weight: bold;
                """);

        contentBox.getChildren().addAll(progressIndicator, messageLabel);
        getChildren().add(contentBox);
        setVisible(false);
    }

    public void updateMessage(String message) {
        if (Platform.isFxApplicationThread()) {
            messageLabel.setText(message);
        } else {
            Platform.runLater(() -> messageLabel.setText(message));
        }
    }

    /**
     * 显示遮罩
     */
    public void show() {
        messageLabel.setText("正在获取文件...");
        setVisible(true);
    }

    /**
     * 隐藏遮罩
     */
    public void hide() {
        setVisible(false);
    }

}