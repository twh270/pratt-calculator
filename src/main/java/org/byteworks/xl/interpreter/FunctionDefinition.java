package org.byteworks.xl.interpreter;

import java.util.Stack;

public class FunctionDefinition {
    private final String name;
    private final Function function;

    public FunctionDefinition(final String name, final Type parameterType, final Type returnType, final FunctionImplementation<Stack<Value>, Value> impl) {
        this.name = name;
        FunctionSignature signature = new FunctionSignature(parameterType, returnType);
        this.function = new Function(signature, impl);
    }

    public FunctionImplementation<Stack<Value>, Value> getImpl() {
        return function.getImpl();
    }

    public Value execute(Stack<Value> stack) {
        return getImpl().apply(stack);
    }

    public FunctionSignature getSignature() {
        return function.getSignature();
    }

    @Override
    public String toString() {
        return name + " = fn" + getSignature();
    }
}
