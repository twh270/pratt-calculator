package org.byteworks.xl;

import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

import org.byteworks.xl.interpreter.Function;
import org.byteworks.xl.interpreter.FunctionImplementation;
import org.byteworks.xl.interpreter.FunctionParameter;
import org.byteworks.xl.interpreter.FunctionSignature;
import org.byteworks.xl.interpreter.Interpreter;
import org.byteworks.xl.interpreter.SimpleType;
import org.byteworks.xl.interpreter.Type;
import org.byteworks.xl.interpreter.TypeList;
import org.byteworks.xl.interpreter.Value;
import org.byteworks.xl.parser.Node;

public class CalculatorInterpreter {
    private static final String TYPE_NUMBER = "Number";

    class InterpretedFunction implements FunctionImplementation {
        private final CalculatorParser.ExpressionNode expression;

        InterpretedFunction(final CalculatorParser.ExpressionNode expression) {
            this.expression = expression;
        }

        @Override
        public Value invoke(final FunctionSignature signature, final Stack<Value> stack) {
            final List<FunctionParameter> parameters = signature.getFunctionParameters();
            for (FunctionParameter param : parameters) {
                Value value = stack.pop();
                checkType(value.getType(), param.getType(), "Parameter " + param.getName() + " requires a " + param.getType() + " but a " + value.getType() + " was supplied");
                interpreter.assignVariableValue(param.getName(), value);
            }
            return evaluateExpression(expression);
        }
    }

    final Interpreter interpreter = new Interpreter();

    private final FunctionImplementation numericAddition = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long leftValue = popValue(stack, number, "binary addition left operand expected %s but got %s");
        Long rightValue = popValue(stack, number, "binary addition right operand expected %s but got %s");
        return new Value(leftValue + rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation numericSubtraction = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long leftValue = popValue(stack, number, "binary subtraction left operand expected %s but got %s");
        Long rightValue = popValue(stack, number, "binary subtraction right operand expected %s but got %s");
        return new Value(leftValue - rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation numericMultiplication = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long leftValue = popValue(stack, number, "binary multiplication left operand expected %s but got %s");
        Long rightValue = popValue(stack, number, "binary multiplication right operand expected %s but got %s");
        return new Value(leftValue * rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation numericDivision = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long leftValue = popValue(stack, number, "binary division left operand expected %s but got %s");
        Long rightValue = popValue(stack, number, "binary division right operand expected %s but got %s");
        return new Value(leftValue / rightValue, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation preIncrement = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long operand = popValue(stack, number, "pre-increment operand expected %s but got %s");
        return new Value(operand + 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation preDecrement = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long operand = popValue(stack, number, "pre-decrement operand expected %s but got %s");
        return new Value(operand - 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation postIncrement = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long operand = popValue(stack, number, "post-increment operand expected %s but got %s");
        return new Value(operand + 1, interpreter.getType(TYPE_NUMBER));
    };
    private final FunctionImplementation postDecrement = (signature, stack) -> {
        Type number = interpreter.getType(TYPE_NUMBER);
        Long operand = popValue(stack, number, "post-decrement operand expected %s but got %s");
        return new Value(operand - 1, interpreter.getType(TYPE_NUMBER));
    };

    public CalculatorInterpreter() {
        Type number = new SimpleType(TYPE_NUMBER);
        interpreter.registerType(TYPE_NUMBER, new SimpleType(TYPE_NUMBER));
        List<FunctionParameter> twoNumbers = List.of(new FunctionParameter("x", interpreter.getType(TYPE_NUMBER)), new FunctionParameter("y", interpreter.getType(TYPE_NUMBER)));
        List<FunctionParameter> oneNumber = List.of(new FunctionParameter("x", interpreter.getType(TYPE_NUMBER)));
        interpreter.registerFunction("add", twoNumbers, number, numericAddition);
        interpreter.registerFunction("subtract", twoNumbers, number, numericSubtraction);
        interpreter.registerFunction("multiply", twoNumbers, number, numericMultiplication);
        interpreter.registerFunction("divide", twoNumbers, number, numericDivision);
        interpreter.registerFunction("preincrement", oneNumber, number, preIncrement);
        interpreter.registerFunction("predecrement", oneNumber, number, preDecrement);
        interpreter.registerFunction("postincrement", oneNumber, number, postIncrement);
        interpreter.registerFunction("postdecrement", oneNumber, number, postDecrement);
    }

    public void exec(List<Node> nodes, PrintStream ps) {
        for (Node node : nodes) {
            if (node instanceof CalculatorParser.ExpressionNode) {
                ps.println(evaluateExpression((CalculatorParser.ExpressionNode) node));
            } else {
                ps.println(node);
            }
        }
    }

    private Value evaluateExpression(final CalculatorParser.ExpressionNode expression) {
        if (expression instanceof CalculatorParser.LiteralNode) {
            return literalExpression((CalculatorParser.LiteralNode) expression);
        } else if (expression instanceof CalculatorParser.IdentifierNode) {
            return identifierExpression((CalculatorParser.IdentifierNode) expression);
        } else if (expression instanceof CalculatorParser.UnaryOpNode) {
            return unaryOperatorExpression((CalculatorParser.UnaryOpNode) expression);
        } else if (expression instanceof CalculatorParser.BinaryOpNode) {
            return binaryOperatorExpression((CalculatorParser.BinaryOpNode) expression);
        } else if (expression instanceof CalculatorParser.FunctionDeclarationNode) {
            return functionDeclaration((CalculatorParser.FunctionDeclarationNode) expression);
        } else if (expression instanceof CalculatorParser.FunctionCallNode) {
            return callFunction((CalculatorParser.FunctionCallNode) expression);
        } else if (expression instanceof CalculatorParser.ExpressionListNode) {
            return expressionList((CalculatorParser.ExpressionListNode) expression);
        }
        throw new IllegalStateException("Don't know how to evaluate expression " + expression);
    }

    private Value expressionList(final CalculatorParser.ExpressionListNode expressionList) {
        Value result = null;
        for(CalculatorParser.ExpressionNode expression: expressionList.getList()) {
            result = evaluateExpression(expression);
        }
        return result;
    }

    private Value callFunction(final CalculatorParser.FunctionCallNode functionCall) {
        String functionName = functionCall.getChars();
        if (functionCall.getArguments() instanceof CalculatorParser.CommaNode) {
            CalculatorParser.CommaNode arguments = (CalculatorParser.CommaNode) functionCall.getArguments();
            Value first = evaluateExpression((CalculatorParser.ExpressionNode) arguments.getLeft());
            Value second = evaluateExpression((CalculatorParser.ExpressionNode) arguments.getRight());
            Type parameterType = new TypeList(List.of(first.getType(), second.getType()));
            Function function = interpreter.getFunction(functionName, parameterType);
            return interpreter.callFunction(function, List.of(first, second));
        }
        throw new IllegalStateException("Don't know how to call function " + functionCall);
    }

    private Value functionDeclaration(CalculatorParser.FunctionDeclarationNode functionDeclaration) {
        CalculatorParser.FunctionSignatureNode functionSignature = functionDeclaration.getFunctionSignature();
        final List<CalculatorParser.TypeExpressionNode> parameterTypes = functionSignature.getParameterTypes();
        List<FunctionParameter> functionParameters =
                parameterTypes.stream()
                .map(it -> new FunctionParameter(
                        it.getTarget().getChars(),
                        interpreter.getType(it.getTypeExpression().getChars()))
                )
                .collect(Collectors.toList());
        final List<CalculatorParser.IdentifierNode> returnTypes = functionSignature.getReturnTypes();
        Type returnType;
        if (returnTypes.size() == 1) {
            returnType = new SimpleType(returnTypes.get(0).getChars());
        } else {
            returnType = new TypeList(returnTypes.stream().map(it -> interpreter.getType(it.getChars())).collect(Collectors.toList()));
        }
        // TODO type of value should be Function
        return new Value(new Function(new FunctionSignature(functionParameters, returnType), new InterpretedFunction(functionDeclaration.getBody())), null);
    }

    private Value functionDeclaration1(CalculatorParser.FunctionDeclarationNode functionDefinition) {
        CalculatorParser.ProducesNode signature = functionDefinition.getTypeSignature();
        Node left = signature.getLeft();
        List<CalculatorParser.TypeExpressionNode> paramList = Collections.emptyList();
        if (left instanceof CalculatorParser.CommaNode) {
            CalculatorParser.CommaNode params = (CalculatorParser.CommaNode) left;
            paramList = List.of((CalculatorParser.TypeExpressionNode)params.getLeft(), (CalculatorParser.TypeExpressionNode)params.getRight());
        }
        // TODO handle a single parameter
        List<FunctionParameter> functionParameters =
                paramList.stream()
                        .map(it -> new FunctionParameter(
                            it.getTarget().getChars(),
                            interpreter.getType(it.getTypeExpression().getChars()))
                        )
                        .collect(Collectors.toList());
        List<Type> paramTypes = paramList.stream()
                .map(CalculatorParser.TypeExpressionNode::getTypeExpression)
                .map(it -> it.getChars())
                .map(interpreter::getType)
                .collect(Collectors.toList());
        Type parameterType = new TypeList(paramTypes);
        // TODO handle compound return type
        Type returnType = interpreter.getType(((CalculatorParser.IdentifierNode) signature.getRight()).getChars());
        return new Value(new Function(new FunctionSignature(functionParameters, returnType), new InterpretedFunction(functionDefinition.getBody())), parameterType);
    }

    private Value binaryOperatorExpression(final CalculatorParser.BinaryOpNode binaryOp) {
        CalculatorParser.ExpressionNode lhs = binaryOp.getLhs();
        CalculatorParser.ExpressionNode rhs = binaryOp.getRhs();
        if (binaryOp instanceof CalculatorParser.AssignmentNode) {
            return callAssignment(binaryOp, (CalculatorParser.IdentifierNode) lhs, rhs);
        }
        Value left = evaluateExpression(lhs);
        Value right = evaluateExpression(rhs);
        if (binaryOp instanceof CalculatorParser.PlusNode) {
            return callBinaryNumericFunction(left, right, "add");
        } else if (binaryOp instanceof CalculatorParser.MinusNode) {
            return callBinaryNumericFunction(left, right, "subtract");
        } else if (binaryOp instanceof CalculatorParser.MultiplyNode) {
            return callBinaryNumericFunction(left, right, "multiply");
        } else if (binaryOp instanceof CalculatorParser.DivideNode) {
            return callBinaryNumericFunction(left, right, "divide");
        } else {
            throw new IllegalStateException("Don't know \nhow to evaluate binary operator " + binaryOp.getClass().getSimpleName() + " in expression " + binaryOp);
        }
    }

    private Value callAssignment(final CalculatorParser.BinaryOpNode binaryOp, final CalculatorParser.IdentifierNode identifierNode, final CalculatorParser.ExpressionNode rhs) {
        if (!(binaryOp.getLhs() instanceof CalculatorParser.IdentifierNode)) {
            throw new IllegalStateException("The left hand side of an assignment must be an identifier in expression " + binaryOp);
        }
        Value value = evaluateExpression(rhs);
        if (value.getValue() instanceof Function) {
            Function function = (Function) value.getValue();
            String functionName = identifierNode.getChars();
            FunctionSignature signature = function.getSignature();
            value = new Value(interpreter.registerFunction(functionName, function.getSignature().getFunctionParameters(), signature.getReturnType(), function.getImpl()),
                    function.getSignature().getParameterType());
        }
        interpreter.assignVariableValue(identifierNode.getChars(), value);
        return value;
    }

    private Value callBinaryNumericFunction(final Value left, final Value right, final String name) {
        Type num = interpreter.getType(TYPE_NUMBER);
        Type binaryFunctionParameterType = new TypeList(List.of(num, num));
        Function fn = interpreter.getFunction(name, binaryFunctionParameterType);
        return interpreter.callFunction(fn, List.of(right, left));
    }

    private Value callUnaryNumericFunction(final Value arg, final String name) {
        Type num = interpreter.getType(TYPE_NUMBER);
        Function fn = interpreter.getFunction(name, num);
        return interpreter.callFunction(fn, List.of(arg));
    }

    private void checkType(Type type, Type expected, String error) {
        if (!expected.equals(type)) {
            throw new IllegalStateException(String.format(error, expected, type));
        }
    }

    private Value unaryOperatorExpression(final CalculatorParser.UnaryOpNode unaryOp) {
        Value operand = evaluateExpression(unaryOp.getExpr());
        checkType(operand.getType(), interpreter.getType(TYPE_NUMBER), "Unary operator expected a %s but got %s in expression " + unaryOp);
        if (unaryOp instanceof CalculatorParser.NegativeSignedNode) {
            return new Value(-((Long) operand.getValue()), interpreter.getType(TYPE_NUMBER));
        } else if (unaryOp instanceof CalculatorParser.PositiveSignedNode) {
            return new Value(operand.getValue(), interpreter.getType(TYPE_NUMBER));
        } else if (unaryOp instanceof CalculatorParser.PreIncrementNode) {
            Value result = callUnaryNumericFunction(operand, "preincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PreDecrementNode) {
            Value result = callUnaryNumericFunction(operand, "predecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return result;
        } else if (unaryOp instanceof CalculatorParser.PostIncrementNode) {
            Value result = callUnaryNumericFunction(operand, "postincrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return operand;
        } else if (unaryOp instanceof CalculatorParser.PostDecrementNode) {
            Value result = callUnaryNumericFunction(operand, "postdecrement");
            if (unaryOp.getExpr() instanceof CalculatorParser.IdentifierNode) {
                interpreter.assignVariableValue(((CalculatorParser.IdentifierNode) unaryOp.getExpr()).getChars(), result);
            }
            return operand;
        } else {
            throw new IllegalStateException("Unknown unary operator " + unaryOp + " in expression " + unaryOp);
        }
    }

    private Value identifierExpression(final CalculatorParser.IdentifierNode expression) {
        String identifierName = expression.getChars();
        return interpreter.identifier(identifierName);
    }

    private Value literalExpression(final CalculatorParser.LiteralNode literal) {
        try {
            Long longValue = Long.parseLong(literal.getValue());
            return new Value(longValue, interpreter.getType(TYPE_NUMBER));
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Unable to parse literal " + literal.getValue() + " in expression " + literal);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T popValue(Stack<Value> stack, Type expectedType, String error) {
        Value value = pop(stack, expectedType, error);
        return (T) value.getValue();
    }

    private Value pop(Stack<Value> stack, Type expectedType, String error) {
        Value value = stack.pop();
        Type actualType = value.getType();
        if (actualType != expectedType) {
            throw new IllegalStateException(String.format(error, expectedType, actualType));
        }
        return value;
    }

}
