package org.byteworks.xl.interpreter;

public class FunctionSignature implements Type {
    private final String name;
    private final Type parameterType;
    private final Type returnType;

    public FunctionSignature(final String name, final Type parameterType, final Type returnType) {
        this.name = name;
        this.parameterType = parameterType;
        this.returnType = returnType;
    }

    public Type getParameterType() {
        return parameterType;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public String name() {
        return name + "(" + parameterType.toString() + " -> " + returnType.toString() + ")";
    }

    @Override
    public String toString() {
        return name();
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
