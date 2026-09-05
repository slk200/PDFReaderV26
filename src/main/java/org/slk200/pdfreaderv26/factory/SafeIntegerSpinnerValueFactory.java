package org.slk200.pdfreaderv26.factory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.converter.IntegerStringConverter;

public class SafeIntegerSpinnerValueFactory extends SpinnerValueFactory<Integer> {

    private final int min;
    private final int max;
    private final int defaultValue;
    private final ObjectProperty<Integer> value = new SimpleObjectProperty<Integer>(this, "value") {
        @Override
        public void set(Integer newValue) {
            if (newValue == null) {
                newValue = defaultValue;
            }
            super.set(newValue);
        }
    };

    public SafeIntegerSpinnerValueFactory(int min, int max, int initialValue) {
        this(min, max, initialValue, 1);
    }

    public SafeIntegerSpinnerValueFactory(int min, int max, int initialValue, int amountToStepBy) {
        this.min = min;
        this.max = max;
        this.defaultValue = initialValue;
        setAmountToStepBy(amountToStepBy);
        setConverter(new IntegerStringConverter());
        setValue(clamp(initialValue));
    }

    private int getAmountToStepBy() {
        return value.get();
    }

    private void setAmountToStepBy(int amountToStepBy) {
        value.set(amountToStepBy);
    }

    @Override
    public void decrement(int steps) {
        Integer current = getValue();
        int newVal = (current != null ? current : defaultValue) - steps * getAmountToStepBy();
        setValue(clamp(newVal));
    }

    @Override
    public void increment(int steps) {
        Integer current = getValue();
        int newVal = (current != null ? current : defaultValue) + steps * getAmountToStepBy();
        setValue(clamp(newVal));
    }

    private int clamp(int val) {
        return Math.max(min, Math.min(max, val));
    }

}