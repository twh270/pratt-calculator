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
    private final Map<String, Map<FunctionSignature, FunctionDefinition>> functions = new HashMap<>();
    private final Stack<Value> stack = new Stack<>();
    private final Map<String, Type> types = new HashMap<>();

    public FunctionDefinition registerFunctionDefinition(String name, Type parameterType, Type returnType, FunctionImplementation<Stack<Value>, Value> impl) {
        FunctionDefinition def = new FunctionDefinition(name, parameterType, returnType, impl);
        Map<FunctionSignature, FunctionDefinition> bound = functions.get(name);
        if (bound == null) {
            bound = new HashMap<>();
            functions.put(name, bound);
        }
        bound.put(def.getSignature(), def);
        return def;
    }

    public FunctionDefinition getFunctionDefinition(final String name, final FunctionSignature signature) {
        Map<FunctionSignature, FunctionDefinition> bound = functions.get(name);
        FunctionDefinition functionDefinition = bound.get(signature);
        if (functionDefinition == null) {
            throw new IllegalArgumentException("Could not find function definition for function name '" + name + "' with signature '" + signature + "'");
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

}
