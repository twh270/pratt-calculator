package org.byteworks.xl.interpreter;

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
}
