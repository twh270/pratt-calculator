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
    private final List<Type> oneNumeric = List.of(Type.NUMBER);

    private final FunctionDefinition numericAddition = new FunctionDefinition("add", twoNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, Type.NUMBER, "binary addition", "left");
        checkOperandType(right, Type.NUMBER, "binary addition", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue + rightValue, Type.NUMBER);
    });
    private final FunctionDefinition numericSubtraction = new FunctionDefinition("subtract", twoNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, Type.NUMBER, "binary subtraction", "left");
        checkOperandType(right, Type.NUMBER, "binary subtraction", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue - rightValue, Type.NUMBER);
    });
    private final FunctionDefinition numericMultiplication = new FunctionDefinition("multiply", twoNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, Type.NUMBER, "binary multiplication", "left");
        checkOperandType(right, Type.NUMBER, "binary multiplication", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue * rightValue, Type.NUMBER);
    });
    private final FunctionDefinition numericDivision = new FunctionDefinition("divide", twoNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, Type.NUMBER, "binary division", "left");
        checkOperandType(right, Type.NUMBER, "binary division", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue / rightValue, Type.NUMBER);
    });
    private final FunctionDefinition preIncrement = new FunctionDefinition("preincrement", oneNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, Type.NUMBER, "pre-increment", "operand");
        return new Value((Long) operand.getValue() + 1, Type.NUMBER);
    });
    private final FunctionDefinition preDecrement = new FunctionDefinition("predecrement", oneNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, Type.NUMBER, "pre-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, Type.NUMBER);
    });
    private final FunctionDefinition postIncrement = new FunctionDefinition("postincrement", oneNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, Type.NUMBER, "post-increment", "operand");
        return new Value((Long) operand.getValue() + 1, Type.NUMBER);
    });
    private final FunctionDefinition postDecrement = new FunctionDefinition("postdecrement", oneNumeric, Type.NUMBER, (FunctionImplementation<Stack<Value>, Value>) stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, Type.NUMBER, "post-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, Type.NUMBER);
    });

    public CalculatorInterpreter() {
        functions.put(numericAddition.getSignature(), numericAddition);
        functions.put(numericSubtraction.getSignature(), numericSubtraction);
        functions.put(numericMultiplication.getSignature(), numericMultiplication);
        functions.put(numericDivision.getSignature(), numericDivision);
        functions.put(preIncrement.getSignature(), preIncrement);
        functions.put(preDecrement.getSignature(), preDecrement);
        functions.put(postIncrement.getSignature(), postIncrement);
        functions.put(postDecrement.getSignature(), postDecrement);
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
                return obj.toString().equals(this.toString());
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
            Value value = evaluateExpression(rhs);
            CalculatorParser.IdentifierNode ident = (CalculatorParser.IdentifierNode) binaryOp.getLhs();
            assignVariableValue(ident, value);
            return value;
        }
        Value left = evaluateExpression(lhs);
        Value right = evaluateExpression(rhs);
        if (binaryOp instanceof CalculatorParser.PlusNode) {
            return callBinaryNumericFunction(left, right, "add");
        } else if (binaryOp instanceof CalculatorParser.MinusNode) {
            return callBinaryNumericFunction(left, right, "subtract");
        } else if (binaryOp instanceof CalculatorParser.MultNode) {
            return callBinaryNumericFunction(left, right, "multiply");
        } else if (binaryOp instanceof CalculatorParser.DivideNode) {
            return callBinaryNumericFunction(left, right, "divide");
        } else {
            throw new IllegalStateException("Don't know \nhow to evaluate binary operator " + expression.getClass().getSimpleName() + " in expression " + expression);
        }
    }

    private void assignVariableValue(CalculatorParser.IdentifierNode ident, Value value) {
        heap.put(ident.getChars(), value);
    }

    private Value callBinaryNumericFunction(final Value left, final Value right, final String name) {
        FunctionDefinition fn = getFunction(name, twoNumeric, Type.NUMBER);
        stack.push(right);
        stack.push(left);
        return fn.execute(stack);
    }

    private Value callUnaryNumericFunction(final Value arg, final String name) {
        FunctionDefinition fn = getFunction(name, oneNumeric, Type.NUMBER);
        stack.push(arg);
        return fn.execute(stack);
    }

    private FunctionDefinition getFunction(String name, List<Type> parameterTypes, Type returnType) {
        FunctionSignature signature = new FunctionSignature(name, parameterTypes, returnType);
        FunctionDefinition fn = functions.get(signature);
        if (fn == null) {
            throw new IllegalArgumentException("Could not find function matching signature '" + signature + "'");
        }
        return fn;
    }

    private Value unaryOperatorExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.UnaryOpNode unaryOp = (CalculatorParser.UnaryOpNode) expression;
        Value operand = evaluateExpression(unaryOp.getExpr());
        if (operand.getType() != Type.NUMBER) {
            throw new IllegalStateException("Unary operator expected a number but got " + operand + " in expression " + expression);
        }
        if (unaryOp instanceof CalculatorParser.NegativeSigned) {
            return new Value(-((Long) operand.getValue()), Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PositiveSigned) {
            return new Value(operand.getValue(), Type.NUMBER);
        } else if (unaryOp instanceof CalculatorParser.PreIncrement) {
            Value result = callUnaryNumericFunction(operand, "preincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                assignVariableValue((CalculatorParser.IdentifierNode) unaryOp.getExpr(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PreDecrement) {
            Value result = callUnaryNumericFunction(operand, "predecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                assignVariableValue((CalculatorParser.IdentifierNode) unaryOp.getExpr(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PostIncrement) {
            Value result = callUnaryNumericFunction(operand, "postincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                assignVariableValue((CalculatorParser.IdentifierNode) unaryOp.getExpr(), result);
            }
            return operand;
        } else if (unaryOp instanceof CalculatorParser.PostDecrement) {
            Value result = callUnaryNumericFunction(operand, "postdecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                assignVariableValue((CalculatorParser.IdentifierNode) unaryOp.getExpr(), result);
            }
            return operand;
        } else {
            throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + expression);
        }
    }

    private Value identifierExpression(final CalculatorParser.IdentifierNode expression) {
        String identName = expression.getChars();
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

    private void checkOperandType(Value value, Type expected, String operatorName, String operandName) {
        if (value.getType() != expected) {
            throw new IllegalStateException("Operator " + operatorName + " expecting a number for " + operandName + " operand but got a " + value.getType() + " for value " + value.getValue());
        }
    }
}
