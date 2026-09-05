package org.slk200.pdfreaderv26.manager;

import javafx.scene.control.Spinner;
import javafx.scene.control.TextFormatter;
import javafx.util.StringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;

public class SafeSpinnerManager {

    /**
     * 为 IntegerSpinner 安装安全保护，彻底防止空值NPE
     */
    public static void makeIntegerSafe(Spinner<Integer> spinner, int defaultValue) {
        StringConverter<Integer> converter = new IntegerStringConverter();

        TextFormatter<Integer> formatter = new TextFormatter<>(converter, defaultValue, change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) {
                return change;
            }
            try {
                Integer.parseInt(newText);
                return change;
            } catch (NumberFormatException e) {
                return null;
            }
        });

        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().setConverter(converter);
    }

    /**
     * 为 DoubleSpinner 安装安全保护，彻底防止空值NPE
     */
    public static void makeDoubleSafe(Spinner<Double> spinner, double defaultValue) {
        StringConverter<Double> converter = new DoubleStringConverter();

        TextFormatter<Double> formatter = new TextFormatter<>(converter, defaultValue, change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty())
                return change;
            if (newText.matches("\\d*\\.?\\d{0,2}"))
                return change;
            else
                return null;
        });

        spinner.getEditor().setTextFormatter(formatter);
        spinner.getValueFactory().setConverter(converter);
    }
}