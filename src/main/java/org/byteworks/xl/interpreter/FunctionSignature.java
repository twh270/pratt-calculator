package org.byteworks.xl.interpreter;

import java.util.Objects;

public class FunctionSignature implements Type {
    private final Type parameterType;
    private final Type returnType;

    public FunctionSignature(final Type parameterType, final Type returnType) {
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
        return "(" + parameterType.toString() + " -> " + returnType.toString() + ")";
    }

    @Override
    public String toString() {
        return name();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final FunctionSignature signature = (FunctionSignature) o;
        return parameterType.equals(signature.parameterType) && returnType.equals(signature.returnType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterType, returnType);
    }
}
