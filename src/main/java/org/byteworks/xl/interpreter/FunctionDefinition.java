package org.byteworks.xl.interpreter;

import java.util.Stack;

public class FunctionDefinition {
    private final FunctionSignature signature;
    private final FunctionImplementation<Stack<Value>, Value> impl;

    // TODO get rid of the name as part of the signature
    public FunctionDefinition(final String name, final Type parameterType, final Type returnType, final FunctionImplementation<Stack<Value>, Value> impl) {
        this.signature = new FunctionSignature(name, parameterType, returnType);
        this.impl = impl;
    }

    public FunctionImplementation<Stack<Value>, Value> getImpl() {
        return impl;
    }

    public Value execute(Stack<Value> stack) {
        return impl.apply(stack);
    }

    public FunctionSignature getSignature() {
        return signature;
    }

}
