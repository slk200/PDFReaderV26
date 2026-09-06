package org.slk200.pdfreaderv26.factory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.DoubleStringConverter;

public class SafeDoubleSpinnerValueFactory extends SpinnerValueFactory<Double> {

    private final double min;
    private final double max;
    private final double defaultValue;

    private final ObjectProperty<Double> value = new SimpleObjectProperty<>(this, "value") {
        @Override
        public void set(Double newValue) {
            if (newValue == null) {
                newValue = defaultValue;
            }
            super.set(newValue);
        }
    };

    public SafeDoubleSpinnerValueFactory(double min, double max, double initialValue) {
        this(min, max, initialValue, 0.01);
    }

    public SafeDoubleSpinnerValueFactory(double min, double max, double initialValue, double amountToStepBy) {
        this.min = min;
        this.max = max;
        this.defaultValue = initialValue;
        setAmountToStepBy(amountToStepBy);
        setConverter(new DoubleStringConverter());
        setValue(clamp(initialValue));
    }

    private double getAmountToStepBy() {
        return value.get();
    }

    private void setAmountToStepBy(double amountToStepBy) {
        value.set(amountToStepBy);
    }

    @Override
    public void decrement(int steps) {
        Double current = getValue();
        double newVal = (current != null ? current : defaultValue) - steps * getAmountToStepBy();
        setValue(Double.valueOf(String.format("%.2f", clamp(newVal))));
    }

    @Override
    public void increment(int steps) {
        Double current = getValue();
        double newVal = (current != null ? current : defaultValue) + steps * getAmountToStepBy();
        setValue(Double.valueOf(String.format("%.2f", clamp(newVal))));
    }

    private double clamp(double val) {
        return Math.clamp(val, min, max);
    }

}