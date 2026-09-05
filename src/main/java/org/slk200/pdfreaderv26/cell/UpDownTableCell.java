package org.slk200.pdfreaderv26.cell;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.util.Duration;

/**
 * 加减TableCell
 */
public class UpDownTableCell<S> extends TableCell<S, Integer> {

    private final HBox container;
    private final Label valLabel;
    private final Button upButton;
    private final Button downButton;
    private Timeline timeline;

    public UpDownTableCell() {
        valLabel = new Label();
        upButton = new Button("+");
        downButton = new Button("-");
        upButton.setPrefWidth(35);
        downButton.setPrefWidth(35);

        container = new HBox(10);
        container.setAlignment(Pos.CENTER);
        container.getChildren().addAll(downButton, valLabel, upButton);

        allEvent();
    }

    @Override
    protected void updateItem(Integer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setGraphic(null);
            return;
        }
        valLabel.setText(item.toString());
        setGraphic(container);
    }

    public void allEvent() {
        upButton.setOnAction(_ -> increment());
        upButton.setOnMouseReleased(_ -> timeline.stop());
        upButton.setOnMousePressed(_ -> {
            timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), _ -> increment())
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });

        downButton.setOnAction(_ -> decrement());
        downButton.setOnMouseReleased(_ -> timeline.stop());
        downButton.setOnMousePressed(_ -> {
            timeline = new Timeline(
                    new KeyFrame(Duration.millis(500), _ -> decrement())
            );
            timeline.setCycleCount(Timeline.INDEFINITE);
            timeline.play();
        });
    }

    private void increment() {
        int newVal = Integer.parseInt(valLabel.getText());
        valLabel.setText(String.valueOf(++newVal));
        incrementNext(getIndex());
    }

    private void decrement() {
        int newVal = Integer.parseInt(valLabel.getText());

        newVal = newVal - 1;
        if (newVal < 1) {
            newVal = 1;
        } else {
            decrementNext(getIndex());
        }
        valLabel.setText(String.valueOf(newVal));
    }

    public void incrementNext(int index) {

    }

    public void decrementNext(int index) {

    }
}
