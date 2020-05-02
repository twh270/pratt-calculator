package org.byteworks.xl.interpreter;

public class FunctionParameter {
    private final String name;
    private final Type type;

    public FunctionParameter(final String name, final Type type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {
        return name + ":" + type;
    }
}
