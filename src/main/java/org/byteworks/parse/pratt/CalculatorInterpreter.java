package org.byteworks.parse.pratt;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CalculatorInterpreter {
    private final Map<String, Value> heap = new HashMap<>();
    private final Map<FunctionSignature, FunctionDefinition> functions = new HashMap<>();
    private final Stack<Value> stack = new Stack<>();

    private final List<Type> twoNumeric = List.of(Type.NUMBER, Type.NUMBER);
    private final FunctionDefinition numericAddition = new FunctionDefinition("add", twoNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, Type.NUMBER, "binary plus", "right");
        checkOperandType(right, Type.NUMBER, "binary plus", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue + rightValue, Type.NUMBER);
    });

    public CalculatorInterpreter() {
        functions.put(numericAddition.getSignature(), numericAddition);
    }

    public Value getVariable(final String name) {
        return heap.get(name);
    }

    public enum Type {
        NUMBER
    }

    interface FunctionImplementation<T extends Stack<Value>, U extends Value> extends Function<T, U> {
    }

    class FunctionSignature {
        private final String name;
        private final List<Type> parameterTypes;
        private final Type returnType;

        FunctionSignature(final String name, final List<Type> parameterTypes, final Type returnType) {
            this.name = name;
            this.parameterTypes = parameterTypes;
            this.returnType = returnType;
        }

        @Override
        public String toString() {
            return name + "(" + parameterTypes.stream().map(Enum::toString).collect(Collectors.joining(",")) + ") -> " + returnType.toString();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof FunctionSignature) {
                return ((FunctionSignature)obj).toString().equals(this.toString());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return toString().hashCode();
        }
    }

    class FunctionDefinition {
        private final FunctionSignature signature;
        private final FunctionImplementation<Stack<Value>, Value> impl;

        FunctionDefinition(final String name, final List<Type> parameterTypes, final Type returnType, final FunctionImplementation impl) {
            this.signature = new FunctionSignature(name, parameterTypes, returnType);
            this.impl = impl;
        }

        Value execute(Stack stack) {
            return impl.apply(stack);
        }

        public FunctionSignature getSignature() {
            return signature;
        }

        public FunctionImplementation<Stack<Value>, Value> getImpl() {
            return impl;
        }
    }

    public static class Value {
        private final Object value;
        private final Type type;

        public Value(final Object value, final Type type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }

        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return value.toString() + ": " + type;
        }
    }

    public void exec(List<Parser.Node> nodes, PrintStream ps) {
        for (Parser.Node node : nodes) {
            if (node instanceof CalculatorParser.ExpressionNode) {
                ps.println(evaluateExpression((CalculatorParser.ExpressionNode) node));
            }
        }
    }

    private Value evaluateExpression(final CalculatorParser.ExpressionNode expression) {
        if (expression instanceof CalculatorParser.LiteralNode) {
            return literalExpression(expression);
        } else if (expression instanceof CalculatorParser.IdentifierNode) {
            return identifierExpression((CalculatorParser.IdentifierNode) expression);
        } else if (expression instanceof CalculatorParser.UnaryOpNode) {
            return unaryOperatorExpression(expression);
        } else if (expression instanceof CalculatorParser.BinaryOpNode) {
            return binaryOperatorExpression(expression);
        }
        throw new IllegalStateException("Don't know how to evaluate expression " + expression);
    }

    private Value binaryOperatorExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.BinaryOpNode binaryOp = (CalculatorParser.BinaryOpNode) expression;
        if (!(binaryOp.getLhs() instanceof CalculatorParser.ExpressionNode)) {
            throw new IllegalStateException("Expected an expression for lhs of " + expression + " but got " + binaryOp.getLhs().getClass().getSimpleName() + " instead");
        }
        if (!(binaryOp.getRhs() instanceof CalculatorParser.ExpressionNode)) {
            throw new IllegalStateException("Expected an expression for rhs of " + expression + " but got " + binaryOp.getLhs().getClass().getSimpleName() + " instead");
        }
        CalculatorParser.ExpressionNode lhs = (CalculatorParser.ExpressionNode) binaryOp.getLhs();
        CalculatorParser.ExpressionNode rhs = (CalculatorParser.ExpressionNode) binaryOp.getRhs();
        if (binaryOp instanceof CalculatorParser.AssignmentNode) {
            if (!(binaryOp.getLhs() instanceof CalculatorParser.IdentifierNode)) {
                throw new IllegalStateException("The left hand side of an assignment must be a variable in expression " + binaryOp);
            }
            CalculatorParser.IdentifierNode ident = (CalculatorParser.IdentifierNode) binaryOp.getLhs();
            Value value = evaluateExpression(rhs);
            heap.put(ident.getChars(), value);
            return value;
        }
        Value left = evaluateExpression(lhs);
        Value right = evaluateExpression(rhs);
        if (binaryOp instanceof CalculatorParser.PlusNode) {
            FunctionSignature signature = new FunctionSignature("add", twoNumeric, Type.NUMBER);
            FunctionDefinition fn = functions.get(signature);
            stack.push(right);
            stack.push(left);
            return fn.execute(stack);
//            return binaryPlus(left, right);
        } else if (binaryOp instanceof CalculatorParser.MinusNode) {
            return binaryMinus(left, right);
        } else if (binaryOp instanceof CalculatorParser.MultNode) {
            return binaryMult(left, right);
        } else if (binaryOp instanceof CalculatorParser.DivideNode) {
            return binaryDiv(left, right);
        } else {
            throw new IllegalStateException("Don't know how to evaluate binary operator " + expression.getClass().getSimpleName() + " in expression " + expression);
        }
    }

    private Value unaryOperatorExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.UnaryOpNode unaryOp = (CalculatorParser.UnaryOpNode) expression;
        if (!(unaryOp.getExpr() instanceof CalculatorParser.ExpressionNode)) {
            throw new IllegalStateException("Unary operator expected an expression but got " + unaryOp.getExpr() + " in expression " + expression);
        }
        Value value = evaluateExpression((CalculatorParser.ExpressionNode) unaryOp.getExpr());
        if (value.getType() != Type.NUMBER) {
            throw new IllegalStateException("Unary operator expected a number but got " + value + " in expression " + expression);
        }
        if (unaryOp instanceof CalculatorParser.NegativeSigned) {
            return new Value(-((Long) value.getValue()), Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PositiveSigned) {
            return new Value(value.getValue(), Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PreIncrement) {
            CalculatorParser.PreIncrement preIncrement = (CalculatorParser.PreIncrement) unaryOp;
            if (preIncrement.getExpr() instanceof CalculatorParser.IdentifierNode) {
                String variableName = ((CalculatorParser.IdentifierNode) preIncrement.getExpr()).getChars();
                Value variableValue = heap.get(variableName);
                Value updatedValue = new Value((Long) variableValue.getValue() + 1, Type.NUMBER);
                heap.put(variableName, updatedValue);
                return updatedValue;
            }
            return new Value((Long) value.getValue() + 1, Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PreDecrement) {
            CalculatorParser.PreDecrement preDecrement = (CalculatorParser.PreDecrement) unaryOp;
            if (preDecrement.getExpr() instanceof CalculatorParser.IdentifierNode) {
                String variableName = ((CalculatorParser.IdentifierNode) preDecrement.getExpr()).getChars();
                Value variableValue = heap.get(variableName);
                Value updatedValue = new Value((Long) variableValue.getValue() - 1, Type.NUMBER);
                heap.put(variableName, updatedValue);
                return updatedValue;
            }
            return new Value((Long) value.getValue() - 1, Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PostIncrement) {
            CalculatorParser.PostIncrement postIncrement = (CalculatorParser.PostIncrement) unaryOp;
            if (postIncrement.getExpr() instanceof CalculatorParser.IdentifierNode) {
                String variableName = ((CalculatorParser.IdentifierNode) postIncrement.getExpr()).getChars();
                Value variableValue = heap.get(variableName);
                heap.put(variableName, new Value((Long) variableValue.getValue() + 1, Type.NUMBER));
                return variableValue;
            }
            return new Value((Long) value.getValue() + 1, Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PostDecrement) {
            CalculatorParser.PostDecrement postDecrement = (CalculatorParser.PostDecrement) unaryOp;
            if (postDecrement.getExpr() instanceof CalculatorParser.IdentifierNode) {
                String variableName = ((CalculatorParser.IdentifierNode) postDecrement.getExpr()).getChars();
                Value variableValue = heap.get(variableName);
                heap.put(variableName, new Value((Long) variableValue.getValue() - 1, Type.NUMBER));
                return variableValue;
            }
            return new Value((Long) value.getValue() - 1, Type.NUMBER);
        } else {
            throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + expression);
        }
    }

    private Value identifierExpression(final CalculatorParser.IdentifierNode expression) {
        CalculatorParser.IdentifierNode ident = expression;
        String identName = ident.getChars();
        if (!heap.containsKey(identName)) {
            throw new IllegalStateException("Could not resolve variable " + identName);
        }
        return heap.get(identName);
    }

    private Value literalExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.LiteralNode literal = (CalculatorParser.LiteralNode) expression;
        try {
            Long longValue = Long.parseLong(literal.getValue());
            return new Value(longValue, Type.NUMBER);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable to parse literal " + literal.getValue() + " in expression " + expression);
        }
    }

    private Value binaryPlus(Value left, Value right) {
        checkOperandType(left, Type.NUMBER, "binary plus", "left");
        checkOperandType(right, Type.NUMBER, "binary plus", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue + rightValue, Type.NUMBER);
    }

    private Value binaryMinus(Value left, Value right) {
        checkOperandType(left, Type.NUMBER, "binary minus", "left");
        checkOperandType(right, Type.NUMBER, "binary minus", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue - rightValue, Type.NUMBER);
    }

    private Value binaryMult(Value left, Value right) {
        checkOperandType(left, Type.NUMBER, "binary multiply", "left");
        checkOperandType(right, Type.NUMBER, "binary multiply", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue * rightValue, Type.NUMBER);
    }

    private Value binaryDiv(Value left, Value right) {
        checkOperandType(left, Type.NUMBER, "binary division", "left");
        checkOperandType(right, Type.NUMBER, "binary division", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        if (0 == rightValue) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new Value(leftValue / rightValue, Type.NUMBER);
    }

    private void checkOperandType(Value value, Type expected, String operatorName, String operandName) {
        if (value.getType() != expected) {
            throw new IllegalStateException("Operator " + operatorName + " expecting a number for " + operandName + " operand but got a " + value.getType() + " for value " + value.getValue());
        }
    }
}
