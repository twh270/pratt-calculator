package org.byteworks.parse.pratt;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculatorInterpreter {
    private final Map<String, Value> variables = new HashMap<>();

    public Value getVariable(final String name) {
        return variables.get(name);
    }

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
            if (node instanceof CalculatorParser.ExpressionNode) {
                ps.println(evaluateExpression((CalculatorParser.ExpressionNode) node));
            }
        }
    }

    private Value evaluateExpression(final CalculatorParser.ExpressionNode expression) {
        if (expression instanceof CalculatorParser.LiteralNode) {
            CalculatorParser.LiteralNode literal = (CalculatorParser.LiteralNode) expression;
            try {
                Long longValue = Long.parseLong(literal.getValue());
                return new Value(longValue, Type.NUMBER);
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Unable to parse literal " + literal.getValue() + " in expression " + expression);
            }
        } else if (expression instanceof CalculatorParser.IdentifierNode) {
            CalculatorParser.IdentifierNode ident = (CalculatorParser.IdentifierNode) expression;
            String identName = ident.getChars();
            if (!variables.containsKey(identName)) {
                throw new IllegalStateException("Could not resolve variable " + identName);
            }
            Value value = variables.get(identName);
            return value;
        } else if (expression instanceof CalculatorParser.UnaryOpNode) {
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
                    Value variableValue = variables.get(variableName);
                    Value updatedValue = new Value((Long) variableValue.getValue() + 1, Type.NUMBER);
                    variables.put(variableName, updatedValue);
                    return updatedValue;
                }
                return new Value((Long) value.getValue() + 1, Type.NUMBER);
            } else if (unaryOp instanceof CalculatorParser.PreDecrement) {
                CalculatorParser.PreDecrement preDecrement = (CalculatorParser.PreDecrement) unaryOp;
                if (preDecrement.getExpr() instanceof CalculatorParser.IdentifierNode) {
                    String variableName = ((CalculatorParser.IdentifierNode) preDecrement.getExpr()).getChars();
                    Value variableValue = variables.get(variableName);
                    Value updatedValue = new Value((Long) variableValue.getValue() - 1, Type.NUMBER);
                    variables.put(variableName, updatedValue);
                    return updatedValue;
                }
                return new Value((Long) value.getValue() - 1, Type.NUMBER);
            } else {
                throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + expression);
            }
        } else if (expression instanceof CalculatorParser.BinaryOpNode) {
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
                variables.put(ident.getChars(), value);
                return value;
            }
            Value left = evaluateExpression(lhs);
            Value right = evaluateExpression(rhs);
            if (binaryOp instanceof CalculatorParser.PlusNode) {
                return binaryPlus(left, right);
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
