package org.byteworks.xl.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/*
    NOTE: keep this class DECOUPLED from language-specific classes
 */

public class Interpreter {
    private final Map<String, Value> heap = new HashMap<>();
    private final Map<String, Map<Type, Function>> functions = new HashMap<>();
    private final Stack<Value> stack = new Stack<>();
    private final Map<String, Type> types = new HashMap<>();

    public Function registerFunction(String name, List<FunctionParameter> functionParameters, Type returnType, FunctionImplementation impl) {
        Function function = new Function(new FunctionSignature(functionParameters, returnType), impl);
        Map<Type, Function> bound = functions.computeIfAbsent(name, k -> new HashMap<>());
        bound.put(function.getSignature().getParameterType(), function);
        return function;
    }

    public Function getFunction(final String name, final Type parameterType) {
        Map<Type, Function> bound = functions.get(name);
        Function function = bound.get(parameterType);
        if (function == null) {
            throw new IllegalArgumentException("Could not find function named '" + name + "' with parameter(s) '" + parameterType + "'");
        }
        return function;
    }

    public void registerType(final String name, final Type type) {
        types.put(name, type);
    }

    public Type getType(String name) {
        Type type = types.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Could not find type '" + name + "'");
        }
        return type;
    }

    public Value getVariable(final String name) {
        return heap.get(name);
    }

    public void assignVariableValue(String identifierName, Value value) {
        heap.put(identifierName, value);
    }

    public Value callFunction(Function function, List<Value> arguments) {
        arguments.forEach(stack::push);
        return function.invoke(stack);
    }

    public Value identifier(String identifierName) {
        Value ident = heap.get(identifierName);
        if (ident == null) {
            throw new IllegalStateException("Could not resolve variable " + identifierName);
        }
        return ident;
    }

}
