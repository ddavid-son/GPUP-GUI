package app.taskForm;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

public class IntField extends TextField {
    final private IntegerProperty value;
    final private int minValue;
    final private int maxValue;


    public int getValue() {
        return value.getValue();
    }

    public void setValue(int newValue) {
        value.setValue(newValue);
    }

    public IntegerProperty valueProperty() {
        return value;
    }

    protected IntField(int minValue, int maxValue, int initialValue) {
        if (minValue > maxValue)
            throw new IllegalArgumentException(
                    "IntField min value " + minValue + " greater than max value " + maxValue
            );
        if (!((minValue <= initialValue) && (initialValue <= maxValue)))
            throw new IllegalArgumentException(
                    "IntField initialValue " + initialValue + " not between " + minValue + " and " + maxValue
            );

        this.minValue = minValue;
        this.maxValue = maxValue;
        setText(initialValue + "");
        value = new SimpleIntegerProperty(initialValue);

        final IntField intField = this;

        // make sure the value property is clamped to the required range
        // and update the field's text to be in sync with the value.
        value.addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null) {
                intField.setText("0");
            } else {
                if (newValue.intValue() < intField.minValue) {
                    value.setValue(intField.minValue);
                    return;
                }

                if (newValue.intValue() > intField.maxValue) {
                    value.setValue(intField.maxValue);
                    return;
                }

                if (newValue.intValue() == 0 && (textProperty().get() == null || "".equals(textProperty().get()))) {
                    intField.setText("0");
                } else {
                    intField.setText(newValue.toString());
                }
            }
        });

        // restrict key input to numerals.
        this.addEventFilter(KeyEvent.KEY_TYPED, keyEvent -> {
            if (intField.minValue < 0) {
                if (!"-0123456789".contains(keyEvent.getCharacter())) {
                    keyEvent.consume();
                }
            } else {
                if (!"0123456789".contains(keyEvent.getCharacter())) {
                    keyEvent.consume();
                }
            }
        });

        // ensure any entered values lie inside the required range.
        this.textProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue == null || "".equals(newValue) || (intField.minValue < 0 && "-".equals(newValue))) {
                value.setValue(0);
                return;
            }

            final int intValue = Integer.parseInt(newValue);

            if (intField.minValue > intValue || intValue > intField.maxValue) {
                textProperty().setValue(oldValue);
            }

            value.set(Integer.parseInt(textProperty().get()));
        });
    }
}