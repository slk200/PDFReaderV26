package org.slk200.pdfreaderv26.component;

import javafx.animation.Animation;
import javafx.animation.FillTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Cursor;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ToggleSwitch extends StackPane {

    private final Rectangle track;
    private final TranslateTransition transition;
    private final BooleanProperty selected = new SimpleBooleanProperty(false);

    private static final double WIDTH = 30;
    private static final double HEIGHT = 8;
    private static final double THUMB_SIZE = 15;
    private static final double THUMB_OFFSET = (WIDTH - THUMB_SIZE) / 2;
    private static final Color TRACK_COLOR_INACTIVE = new Color(0.875, 0.875, 0.875, 1);
    private static final Color TRACK_COLOR_ACTIVE = new Color(0.1215686, 0.5215686, 0.996, 1);

    public ToggleSwitch() {
        track = new Rectangle(WIDTH, HEIGHT);
        track.setArcWidth(HEIGHT); // 设置圆角
        track.setArcHeight(HEIGHT);
        track.setFill(TRACK_COLOR_INACTIVE);

        Rectangle thumb = new Rectangle(THUMB_SIZE, THUMB_SIZE);
        thumb.setArcWidth(THUMB_SIZE);
        thumb.setArcHeight(THUMB_SIZE);
        thumb.setFill(Color.WHITE);
        thumb.setLayoutX(-THUMB_OFFSET);

        this.getChildren().addAll(track, thumb);

        this.setCursor(Cursor.HAND);

        transition = new TranslateTransition(Duration.millis(200), thumb);

    }

    private void updateVisuals(boolean isSelected) {
        FillTransition fillTransition;
        if (isSelected) {
            transition.setToX(THUMB_OFFSET);
            fillTransition = new FillTransition(
                    Duration.millis(200),
                    track,
                    TRACK_COLOR_INACTIVE,
                    TRACK_COLOR_ACTIVE
            );
        } else {
            transition.setToX(-THUMB_OFFSET);
            fillTransition = new FillTransition(
                    Duration.millis(200),
                    track,
                    TRACK_COLOR_ACTIVE,
                    TRACK_COLOR_INACTIVE
            );
        }

        if (transition.getStatus() == Animation.Status.RUNNING) {
            transition.stop();
            fillTransition.stop();
        }
        transition.playFromStart();
        fillTransition.playFromStart();
    }

    public boolean isSelected() {
        return selected.get();
    }

    public void setSelected(boolean value) {
        selected.set(value);
        updateVisuals(value);
    }

}