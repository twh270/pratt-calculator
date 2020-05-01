package org.byteworks.xl.interpreter;

import java.util.Stack;

public class Function {
    private final FunctionSignature signature;
    private final FunctionImplementation<Stack<Value>, Value> impl;

    public Function(final FunctionSignature signature, final FunctionImplementation<Stack<Value>, Value> impl) {
        this.signature = signature;
        this.impl = impl;
    }

    public FunctionSignature getSignature() {
        return signature;
    }

    public FunctionImplementation<Stack<Value>, Value> getImpl() {
        return impl;
    }
}
