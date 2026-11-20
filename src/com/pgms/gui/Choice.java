package com.pgms.gui;

/** An item for a drop-down: the real value plus the text shown for it. */
public class Choice<T> {

    private final T value;
    private final String text;

    public Choice(T value, String text) {
        this.value = value;
        this.text = text;
    }

    public T value() {
        return value;
    }

    @Override
    public String toString() {
        return text;
    }
}
