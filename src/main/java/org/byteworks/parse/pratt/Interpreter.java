package org.byteworks.parse.pratt;

import java.io.PrintStream;
import java.util.List;

public class Interpreter {

    public enum Type {
        NUMBER
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
            if (node instanceof Parser.ExpressionNode) {
                ps.println(evaluateExpression((Parser.ExpressionNode) node));
            }
        }
    }

    private Value evaluateExpression(final Parser.ExpressionNode expression) {
        if (expression instanceof Parser.LiteralNode) {
            Parser.LiteralNode literal = (Parser.LiteralNode) expression;
            try {
                Long longValue = Long.parseLong(literal.getValue());
                return new Value(longValue, Type.NUMBER);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Unable to parse literal " + literal.getValue() + " in expression " + expression);
            }
        } else if (expression instanceof Parser.UnaryOpNode) {
            Parser.UnaryOpNode unaryOp = (Parser.UnaryOpNode) expression;
            if (!(unaryOp.getExpr() instanceof Parser.ExpressionNode)) {
                throw new IllegalStateException("Unary operator expected an expression but got " + unaryOp.getExpr() + " in expression " + expression);
            }
            Value value = evaluateExpression((Parser.ExpressionNode) unaryOp.getExpr());
            if (value.getType() != Type.NUMBER) {
                throw new IllegalStateException("Unary operator expected a number but got " + value + " in expression " + expression);
            }
            if (unaryOp instanceof Parser.NegativeSigned) {
                return new Value(-((Long) value.getValue()), Type.NUMBER);
            } else if (unaryOp instanceof Parser.PositiveSigned) {
                return new Value(value.getValue(), Type.NUMBER);
            } else {
                throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + expression);
            }
        } else if (expression instanceof Parser.BinaryOpNode) {
            Parser.BinaryOpNode binaryOp = (Parser.BinaryOpNode) expression;
            if (!(binaryOp.getLhs() instanceof Parser.ExpressionNode)) {
                throw new IllegalStateException("Expected an expression for lhs of " + expression + " but got " + binaryOp.getLhs().getClass().getSimpleName() + " instead");
            }
            if (!(binaryOp.getRhs() instanceof Parser.ExpressionNode)) {
                throw new IllegalStateException("Expected an expression for rhs of " + expression + " but got " + binaryOp.getLhs().getClass().getSimpleName() + " instead");
            }
            Parser.ExpressionNode lhs = (Parser.ExpressionNode) binaryOp.getLhs();
            Parser.ExpressionNode rhs = (Parser.ExpressionNode) binaryOp.getRhs();
            Value left = evaluateExpression(lhs);
            Value right = evaluateExpression(rhs);
            if (binaryOp instanceof Parser.PlusNode) {
                return binaryPlus(left, right);
            } else if (binaryOp instanceof Parser.MinusNode) {
                return binaryMinus(left, right);
            } else if (binaryOp instanceof Parser.MultNode) {
                return binaryMult(left, right);
            } else if (binaryOp instanceof Parser.DivideNode) {
                return binaryDiv(left, right);
            } else {
                throw new IllegalStateException("Don't know how to evaluate binary operator " + expression.getClass().getSimpleName() + " in expression " + expression);
            }
        }
        throw new IllegalStateException("Don't know how to evaluate expression " + expression);
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
