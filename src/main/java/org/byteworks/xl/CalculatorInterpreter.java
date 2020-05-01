package org.byteworks.xl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.byteworks.xl.parser.Parser;

public class CalculatorInterpreter {
    private static final String TYPE_NUMBER = "Number";
    private static final String TYPE_TWO_NUMBER = "TwoNumber";

    private final Map<String, Value> heap = new HashMap<>();
    private final Map<FunctionSignature, FunctionDefinition> functions = new HashMap<>();
    private final Stack<Value> stack = new Stack<>();
    private final Map<String, Type> types = new HashMap<>();

    private Type getType(String name) {
        Type type = types.get(name);
        if (type == null) {
            throw new IllegalArgumentException("Could not find type '" + name + "'");
        }
        return type;
    }

    private final FunctionImplementation<Stack<Value>, Value> numericAddition = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, getType(TYPE_NUMBER), "binary addition", "left");
        checkOperandType(right, getType(TYPE_NUMBER), "binary addition", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue + rightValue, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericSubtraction = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, getType(TYPE_NUMBER), "binary subtraction", "left");
        checkOperandType(right, getType(TYPE_NUMBER), "binary subtraction", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue - rightValue, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericMultiplication = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, getType(TYPE_NUMBER), "binary multiplication", "left");
        checkOperandType(right, getType(TYPE_NUMBER), "binary multiplication", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue * rightValue, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericDivision = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, getType(TYPE_NUMBER), "binary division", "left");
        checkOperandType(right, getType(TYPE_NUMBER), "binary division", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue / rightValue, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> preIncrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, getType(TYPE_NUMBER), "pre-increment", "operand");
        return new Value((Long) operand.getValue() + 1, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> preDecrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, getType(TYPE_NUMBER), "pre-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> postIncrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, getType(TYPE_NUMBER), "post-increment", "operand");
        return new Value((Long) operand.getValue() + 1, getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> postDecrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, getType(TYPE_NUMBER), "post-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, getType(TYPE_NUMBER));
    };

    public CalculatorInterpreter() {
        Type number = new SimpleType(TYPE_NUMBER);
        types.put(TYPE_NUMBER, number);
        Type twoNumber = new TypeList(List.of(types.get(TYPE_NUMBER), types.get(TYPE_NUMBER)));
        types.put(TYPE_TWO_NUMBER, twoNumber);
        registerFunction("add", twoNumber, number, numericAddition);
        registerFunction("subtract", twoNumber, number, numericSubtraction);
        registerFunction("multiply", twoNumber, number, numericMultiplication);
        registerFunction("divide", twoNumber, number, numericDivision);
        registerFunction("preincrement", number, number, preIncrement);
        registerFunction("predecrement", number, number, preDecrement);
        registerFunction("postincrement", number, number, postIncrement);
        registerFunction("postdecrement", number, number, postDecrement);
    }

    private void registerFunction(String name, Type parameterType, Type returnType, FunctionImplementation impl) {
        FunctionDefinition def = new FunctionDefinition(name, parameterType, returnType, impl);
        functions.put(def.getSignature(), def);
    }

    public Value getVariable(final String name) {
        return heap.get(name);
    }

    public interface Type {
        String name();
    }

    public class SimpleType implements Type {
        private final String name;

        public SimpleType(final String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final SimpleType type = (SimpleType) o;
            return name.equals(type.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name();
        }
    }

    public class TypeList implements Type {
        private final List<Type> types;
        private final String name;

        public TypeList(final List<Type> types) {
            this.types = new ArrayList<>(types);
            name = types.stream().map(Type::name).collect(Collectors.joining(", "));
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final TypeList list = (TypeList) o;
            return name.equals(list.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }
    }

    interface FunctionImplementation<T extends Stack<Value>, U extends Value> extends Function<T, U> {
    }

    class FunctionSignature {
        private final String name;
        private final Type parameterType;
        private final Type returnType;

        FunctionSignature(final String name, final Type parameterType, final Type returnType) {
            this.name = name;
            this.parameterType = parameterType;
            this.returnType = returnType;
        }

        @Override
        public String toString() {
            return name + "(" + parameterType.toString() + ") -> " + returnType.toString();
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

        FunctionDefinition(final String name, final Type parameterType, final Type returnType, final FunctionImplementation impl) {
            this.signature = new FunctionSignature(name, parameterType, returnType);
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
        FunctionDefinition fn = getFunction(name, TYPE_TWO_NUMBER, TYPE_NUMBER);
        stack.push(right);
        stack.push(left);
        return fn.execute(stack);
    }

    private Value callUnaryNumericFunction(final Value arg, final String name) {
        FunctionDefinition fn = getFunction(name, TYPE_NUMBER, TYPE_NUMBER);
        stack.push(arg);
        return fn.execute(stack);
    }

    private FunctionDefinition getFunction(String name, String parameterType, String returnType) {
        FunctionSignature signature = new FunctionSignature(name, getType(parameterType), getType(returnType));
        FunctionDefinition fn = functions.get(signature);
        if (fn == null) {
            throw new IllegalArgumentException("Could not find function matching signature '" + signature + "'");
        }
        return fn;
    }

    private void checkType(Type type, Type expected, String error) {
        if (!expected.equals(type)) {
            throw new IllegalStateException(String.format(error, expected, type));
        }
    }

    private Value unaryOperatorExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.UnaryOpNode unaryOp = (CalculatorParser.UnaryOpNode) expression;
        Value operand = evaluateExpression(unaryOp.getExpr());
        checkType(operand.getType(), getType(TYPE_NUMBER), "Unary operator expected a %s but got %s in expression " + expression);
        if (unaryOp instanceof CalculatorParser.NegativeSigned) {
            return new Value(-((Long) operand.getValue()), getType(TYPE_NUMBER));
        } else if (unaryOp instanceof CalculatorParser.PositiveSigned) {
            return new Value(operand.getValue(), getType(TYPE_NUMBER));
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
            return new Value(longValue, getType(TYPE_NUMBER));
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
