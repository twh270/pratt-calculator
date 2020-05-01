package org.byteworks.xl;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.byteworks.xl.interpreter.Function;
import org.byteworks.xl.interpreter.FunctionDefinition;
import org.byteworks.xl.interpreter.FunctionImplementation;
import org.byteworks.xl.interpreter.FunctionSignature;
import org.byteworks.xl.interpreter.Interpreter;
import org.byteworks.xl.interpreter.SimpleType;
import org.byteworks.xl.interpreter.Type;
import org.byteworks.xl.interpreter.TypeList;
import org.byteworks.xl.interpreter.Value;
import org.byteworks.xl.parser.Node;

public class CalculatorInterpreter {
    private static final String TYPE_NUMBER = "Number";
    private static final String TYPE_TWO_NUMBERS = "TwoNumbers";

    final Interpreter interpreter = new Interpreter();

    private final FunctionImplementation<Stack<Value>, Value> numericAddition = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, interpreter.getType(TYPE_NUMBER), "binary addition", "left");
        checkOperandType(right, interpreter.getType(TYPE_NUMBER), "binary addition", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue + rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericSubtraction = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, interpreter.getType(TYPE_NUMBER), "binary subtraction", "left");
        checkOperandType(right, interpreter.getType(TYPE_NUMBER), "binary subtraction", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue - rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericMultiplication = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, interpreter.getType(TYPE_NUMBER), "binary multiplication", "left");
        checkOperandType(right, interpreter.getType(TYPE_NUMBER), "binary multiplication", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue * rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> numericDivision = stack -> {
        Value left = stack.pop();
        Value right = stack.pop();
        checkOperandType(left, interpreter.getType(TYPE_NUMBER), "binary division", "left");
        checkOperandType(right, interpreter.getType(TYPE_NUMBER), "binary division", "right");
        Long leftValue = (Long) left.getValue();
        Long rightValue = (Long) right.getValue();
        return new Value(leftValue / rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> preIncrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, interpreter.getType(TYPE_NUMBER), "pre-increment", "operand");
        return new Value((Long) operand.getValue() + 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> preDecrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, interpreter.getType(TYPE_NUMBER), "pre-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> postIncrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, interpreter.getType(TYPE_NUMBER), "post-increment", "operand");
        return new Value((Long) operand.getValue() + 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation<Stack<Value>, Value> postDecrement = stack -> {
        Value operand = stack.pop();
        checkOperandType(operand, interpreter.getType(TYPE_NUMBER), "post-decrement", "operand");
        return new Value((Long) operand.getValue() - 1, interpreter.getType(TYPE_NUMBER));
    };

    public CalculatorInterpreter() {
        Type number = new SimpleType(TYPE_NUMBER);
        interpreter.registerType(TYPE_NUMBER, number);
        Type twoNumbers = new TypeList(List.of(interpreter.getType(TYPE_NUMBER), interpreter.getType(TYPE_NUMBER)));
        interpreter.registerType(TYPE_TWO_NUMBERS, twoNumbers);
        interpreter.registerFunctionDefinition("add", twoNumbers, number, numericAddition);
        interpreter.registerFunctionDefinition("subtract", twoNumbers, number, numericSubtraction);
        interpreter.registerFunctionDefinition("multiply", twoNumbers, number, numericMultiplication);
        interpreter.registerFunctionDefinition("divide", twoNumbers, number, numericDivision);
        interpreter.registerFunctionDefinition("preincrement", number, number, preIncrement);
        interpreter.registerFunctionDefinition("predecrement", number, number, preDecrement);
        interpreter.registerFunctionDefinition("postincrement", number, number, postIncrement);
        interpreter.registerFunctionDefinition("postdecrement", number, number, postDecrement);
    }

    public void exec(List<Node> nodes, PrintStream ps) {
        for (Node node : nodes) {
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
        } else if (expression instanceof CalculatorParser.FunctionDefinition) {
            return functionDefinition((CalculatorParser.FunctionDefinition) expression);
        }
        throw new IllegalStateException("Don't know how to evaluate expression " + expression);
    }

    private Value functionDefinition(CalculatorParser.FunctionDefinition functionDefinition) {
        CalculatorParser.ProducesNode signature = functionDefinition.getTypeSignature();
        Node left = signature.getLeft();
        List<CalculatorParser.TypeExpression> paramList = Collections.emptyList();
        if (left instanceof CalculatorParser.CommaNode) {
            CalculatorParser.CommaNode params = (CalculatorParser.CommaNode) left;
            paramList = List.of((CalculatorParser.TypeExpression)params.getLeft(), (CalculatorParser.TypeExpression)params.getRight());
        }
        // TODO handle a single parameter
        List<Type> paramTypes = paramList.stream()
                .map(CalculatorParser.TypeExpression::getTypeExpression)
                .map(it -> ((CalculatorParser.IdentifierNode) it).getChars())
                .map(interpreter::getType)
                .collect(Collectors.toList());
        Type parameterType = new TypeList(paramTypes);
        Type returnType = interpreter.getType(((CalculatorParser.IdentifierNode) signature.getRight()).getChars());
        FunctionSignature functionSignature = new FunctionSignature(parameterType, returnType);
        return new Value(new Function(new FunctionSignature(parameterType, returnType), new InterpretedFunction(functionDefinition.getBody())), functionSignature);
    }

    class InterpretedFunction implements FunctionImplementation<Stack<Value>, Value> {
        private final Node expression;

        InterpretedFunction(final Node expression) {
            this.expression = expression;
        }

        @Override
        public Value apply(final Stack<Value> values) {
            return evaluateExpression((CalculatorParser.ExpressionNode) expression);
        }
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
            if (value.getValue() instanceof Function) {
                Function function = (Function) value.getValue();
                String functionName = ((CalculatorParser.IdentifierNode)lhs).getChars();
                FunctionSignature signature = function.getSignature();
                value = new Value(interpreter.registerFunctionDefinition(functionName, signature.getParameterType(), signature.getReturnType(), function.getImpl()), function.getSignature());
            }
            interpreter.assignVariableValue(ident.getChars(), value);
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

    private Value callBinaryNumericFunction(final Value left, final Value right, final String name) {
        FunctionDefinition fn = getFunction(name, TYPE_TWO_NUMBERS, TYPE_NUMBER);
        return interpreter.callFunction(fn, List.of(right, left));
    }

    private Value callUnaryNumericFunction(final Value arg, final String name) {
        FunctionDefinition fn = getFunction(name, TYPE_NUMBER, TYPE_NUMBER);
        return interpreter.callFunction(fn, List.of(arg));
    }

    private FunctionDefinition getFunction(String name, String parameterType, String returnType) {
        FunctionSignature signature = new FunctionSignature(interpreter.getType(parameterType), interpreter.getType(returnType));
        return interpreter.getFunctionDefinition(name, signature);
    }

    private void checkType(Type type, Type expected, String error) {
        if (!expected.equals(type)) {
            throw new IllegalStateException(String.format(error, expected, type));
        }
    }

    private Value unaryOperatorExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.UnaryOpNode unaryOp = (CalculatorParser.UnaryOpNode) expression;
        Value operand = evaluateExpression(unaryOp.getExpr());
        checkType(operand.getType(), interpreter.getType(TYPE_NUMBER), "Unary operator expected a %s but got %s in expression " + expression);
        if (unaryOp instanceof CalculatorParser.NegativeSigned) {
            return new Value(-((Long) operand.getValue()), interpreter.getType(TYPE_NUMBER));
        } else if (unaryOp instanceof CalculatorParser.PositiveSigned) {
            return new Value(operand.getValue(), interpreter.getType(TYPE_NUMBER));
        } else if (unaryOp instanceof CalculatorParser.PreIncrement) {
            Value result = callUnaryNumericFunction(operand, "preincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PreDecrement) {
            Value result = callUnaryNumericFunction(operand, "predecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PostIncrement) {
            Value result = callUnaryNumericFunction(operand, "postincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return operand;
        } else if (unaryOp instanceof CalculatorParser.PostDecrement) {
            Value result = callUnaryNumericFunction(operand, "postdecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return operand;
        } else {
            throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + expression);
        }
    }

    private Value identifierExpression(final CalculatorParser.IdentifierNode expression) {
        String identifierName = expression.getChars();
        return interpreter.identifier(identifierName);
    }

    private Value literalExpression(final CalculatorParser.ExpressionNode expression) {
        CalculatorParser.LiteralNode literal = (CalculatorParser.LiteralNode) expression;
        try {
            Long longValue = Long.parseLong(literal.getValue());
            return new Value(longValue, interpreter.getType(TYPE_NUMBER));
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
