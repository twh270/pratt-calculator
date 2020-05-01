package org.byteworks.xl.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Interpreter {
    private final Map<String, Value> heap = new HashMap<>();
    private final Map<FunctionSignature, FunctionDefinition> functions = new HashMap<>();
    private final Stack<Value> stack = new Stack<>();
    private final Map<String, Type> types = new HashMap<>();


    public void registerFunctionDefinition(String name, Type parameterType, Type returnType, FunctionImplementation<Stack<Value>, Value> impl) {
        FunctionDefinition def = new FunctionDefinition(name, parameterType, returnType, impl);
        functions.put(def.getSignature(), def);
    }

    public FunctionDefinition getFunctionDefinition(final FunctionSignature signature) {
        FunctionDefinition functionDefinition = functions.get(signature);
        if (functionDefinition == null) {
            throw new IllegalArgumentException("Could not find function definition for '" + signature + "'");
        }
        return functionDefinition;
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

    public Value callFunction(FunctionDefinition function, List<Value> arguments) {
        arguments.forEach(stack::push);
        return function.execute(stack);
    }

    public Value identifier(String identifierName) {
        Value ident = heap.get(identifierName);
        if (ident == null) {
            throw new IllegalStateException("Could not resolve variable " + identifierName);
        }
        return ident;
    }

    public void registerFunctionDefinition(final String name, final FunctionDefinition definition) {
        registerFunctionDefinition(name, definition.getSignature().getParameterType(), definition.getSignature().getReturnType(), definition.getImpl());
    }
}
