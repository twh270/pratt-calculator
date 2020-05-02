package org.byteworks.xl.interpreter;

import java.util.Stack;

public class Function {
    private final FunctionSignature signature;
    private final FunctionImplementation impl;

    public Function(final FunctionSignature signature, final FunctionImplementation impl) {
        this.signature = signature;
        this.impl = impl;
    }

    public FunctionSignature getSignature() {
        return signature;
    }

    public FunctionImplementation getImpl() {
        return impl;
    }

    public Value invoke(final Stack<Value> values) {
        return impl.invoke(signature, values);
    }

    @Override
    public String toString() {
        return signature.toString();
    }
}
