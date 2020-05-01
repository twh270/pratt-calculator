package org.byteworks.xl.interpreter;

import java.util.Stack;

public class FunctionDefinition {
    private final FunctionSignature signature;
    private final FunctionImplementation<Stack<Value>, Value> impl;

    FunctionDefinition(final String name, final Type parameterType, final Type returnType, final FunctionImplementation<Stack<Value>, Value> impl) {
        this.signature = new FunctionSignature(name, parameterType, returnType);
        this.impl = impl;
    }

    Value execute(Stack<Value> stack) {
        return impl.apply(stack);
    }

    FunctionSignature getSignature() {
        return signature;
    }

}
