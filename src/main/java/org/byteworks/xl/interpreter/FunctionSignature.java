package org.byteworks.xl.interpreter;

public class FunctionSignature {
    private final String name;
    private final Type parameterType;
    private final Type returnType;

    public FunctionSignature(final String name, final Type parameterType, final Type returnType) {
        this.name = name;
        this.parameterType = parameterType;
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return name + "(" + parameterType.toString() + ") -> " + returnType.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof FunctionSignature) {
            return obj.toString().equals(this.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }
}
