package org.byteworks.xl.interpreter;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FunctionSignature {
    private final List<FunctionParameter> functionParameters;
    private final Type parameterType;
    private final Type returnType;

    public FunctionSignature(final List<FunctionParameter> functionParameters, final Type returnType) {
        this.functionParameters = functionParameters;
        if (functionParameters.size() == 1) {
            this.parameterType = functionParameters.get(0).getType();
        } else {
            this.parameterType = new TypeList(functionParameters.stream().map(it -> it.getType()).collect(Collectors.toList()));
        }
        this.returnType = returnType;
    }

    public List<FunctionParameter> getFunctionParameters() {
        return functionParameters;
    }

    public Type getParameterType() {
        return parameterType;
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        return "(" + parameterType.toString() + " -> " + returnType.toString() + ")";
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
