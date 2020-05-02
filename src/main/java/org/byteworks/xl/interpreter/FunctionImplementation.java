package org.byteworks.xl.interpreter;

import java.util.Stack;

public interface FunctionImplementation {
    Value invoke(FunctionSignature signature, Stack<Value> stack);
}
