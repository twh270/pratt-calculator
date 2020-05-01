package org.byteworks.xl.interpreter;

public class Value {
    private final Object value;
    private final Type type;

    public Value(final Object value, final Type type) {
        this.value = value;
        this.type = type;
    }

    public Object getValue() {
        return value;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return value.toString() + ": " + type;
    }
}
