package org.slk200.pdfreaderv26.manager;

import javafx.scene.control.Spinner;

public class SpinnerEditorManager {
    public static <T> void safeSpinner(Spinner<T> spinner, T defaultValue) {
        spinner.getEditor().focusedProperty().addListener((_, _, isFocused) -> {
            if (!isFocused) {
                String text = spinner.getEditor().getText();
                if (text == null || text.trim().isEmpty()) {
                    spinner.getValueFactory().setValue(defaultValue);
                }
            }
        });
    }
}