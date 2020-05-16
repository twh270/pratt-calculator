package org.byteworks.xl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.NodeList;
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.rule.Any;
import org.byteworks.xl.parser.rule.Compose;
import org.byteworks.xl.parser.rule.Constant;
import org.byteworks.xl.parser.rule.Convert;
import org.byteworks.xl.parser.rule.FromToken;
import org.byteworks.xl.parser.rule.PassThrough;
import org.byteworks.xl.parser.rule.Require;
import org.byteworks.xl.parser.rule.RequireNode;
import org.byteworks.xl.parser.rule.RequireWithTerminator;
import org.byteworks.xl.parser.rule.Sequence;

public class XLParser extends Parser {

    public XLParser(final Lexer lexer, final PrintStream debugStream) {
        super(lexer, debugStream);
    }

    public static XLParser createParser(Lexer lexer, PrintStream debugStream) {
        XLParser parser = new XLParser(lexer, debugStream);
        parser.registerPrefixParserRule(TokenType.EOL, eolParser);
        parser.registerPrefixParserRule(TokenType.LPAREN, lparenParser);
        parser.registerPrefixParserRule(TokenType.NUMBER, numberNodeParser);
        parser.registerPrefixParserRule(TokenType.RPAREN, constantEmpty);
        parser.registerPrefixParserRule(TokenType.MINUS, minusNodeParser);
        parser.registerPrefixParserRule(TokenType.PLUS, plusNodeParser);
        parser.registerPrefixParserRule(TokenType.MINUSMINUS, preDecrementNodeParser);
        parser.registerPrefixParserRule(TokenType.PLUSPLUS, preIncrementNodeParser);
        parser.registerPrefixParserRule(TokenType.IDENTIFIER, identNodeParser);
        parser.registerPrefixParserRule(TokenType.FUNCTION_DEFINITION, functionDeclarationNodeParser);
        parser.registerPrefixParserRule(TokenType.LBRACE, leftBraceNodeParser);

        parser.registerInfixParserRule(TokenType.PLUS, parseAddNode);
        parser.registerInfixParserRule(TokenType.PLUSPLUS, postIncrementParser);
        parser.registerInfixParserRule(TokenType.MINUS, subtractNodeParser);
        parser.registerInfixParserRule(TokenType.MINUSMINUS, postDecrementNodeParser);
        parser.registerInfixParserRule(TokenType.MULTIPLY, multiplyNodeParser);
        parser.registerInfixParserRule(TokenType.DIVIDE, divideNodeParser);
        parser.registerInfixParserRule(TokenType.ASSIGNMENT, assignmentNodeParser);
        parser.registerInfixParserRule(TokenType.COMMA, commaNodeParser);
        parser.registerInfixParserRule(TokenType.RPAREN, rightParenNodeParser);
        parser.registerInfixParserRule(TokenType.LPAREN, functionCallNodeParser);
        parser.registerInfixParserRule(TokenType.EOL, endOfLineParser);
        parser.registerInfixParserRule(TokenType.EOF, eofNodeParser);
        parser.registerInfixParserRule(TokenType.COLON, colonNodeParser);
        parser.registerInfixParserRule(TokenType.IDENTIFIER, identifierNodeParser);
        parser.registerInfixParserRule(TokenType.ARROW, arrowNodeParser);
        parser.registerInfixParserRule(TokenType.LBRACE, leftBraceInfixNodeParser);
        parser.registerInfixParserRule(TokenType.RBRACE, rightBraceInfixNodeParser);

        return parser;
    }

    private static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
    private static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
    private static final Pair<Integer, Integer> PARENS = new Pair<>(1, 0);
    private static final Pair<Integer, Integer> BRACES = new Pair<>(-1, 0);
    private static final Pair<Integer, Integer> COMMA = new Pair<>(1, 2);
    private static final Pair<Integer, Integer> ARROW = new Pair<>(1, 2);
    private static final Pair<Integer, Integer> ASSIGNMENT = new Pair<>(3, 4);
    private static final Pair<Integer, Integer> PLUS_MINUS = new Pair<>(5, 6);
    private static final Pair<Integer, Integer> MULT_DIV = new Pair<>(7, 8);
    private static final Pair<Integer, Integer> SIGNED = new Pair<>(null, 10);
    private static final Pair<Integer, Integer> PRE_INCREMENT = new Pair<>(null, 11);
    private static final Pair<Integer, Integer> PRE_DECREMENT = new Pair<>(null, 11);
    private static final Pair<Integer, Integer> POST_INCREMENT = new Pair<>(11, null);
    private static final Pair<Integer, Integer> POST_DECREMENT = new Pair<>(11, null);
    private static final Pair<Integer, Integer> COLON = new Pair<>(11, 12);
    private static final Pair<Integer, Integer> IDENTIFIER = new Pair<>(11, 12);

    // Prefix parsers


    private static final Constant<EmptyNode> constantEmpty = new Constant<>(new EmptyNode());

    private static final Any<Node> eolParser = new Any<>(EOL.getRight());

    private static final FromToken<LiteralNode> numberNodeParser = new FromToken<>(LiteralNode::new);

    private static final Convert<Node, ExpressionNode, NegativeSignedNode> minusNodeParser = new Convert<>(new Require<>(SIGNED.getRight(), ExpressionNode.class, "Must provide an " +
            "expression for negative-signed"), NegativeSignedNode::new);

    private static final Convert<Node, ExpressionNode, PositiveSignedNode> plusNodeParser = new Convert<>(new Require<>(SIGNED.getRight(), ExpressionNode.class, "Must provide an " +
            "expression for positive-signed"), PositiveSignedNode::new);

    private static final Convert<Node, ExpressionNode, PreDecrementNode> preDecrementNodeParser = new Convert<>(new Require<>(PRE_DECREMENT.getRight(), ExpressionNode.class, "Must provide" +
            " an expression for pre-decrement"), PreDecrementNode::new);

    private static final Convert<Node, ExpressionNode, PreIncrementNode> preIncrementNodeParser = new Convert<>(new Require<>(PRE_INCREMENT.getRight(), ExpressionNode.class, "Must provide" +
            " an expression for pre-increment"), PreIncrementNode::new);

    private static final Any<Node> lparenParser = new Any<>(PARENS.getRight());

    private static final FromToken<IdentifierNode> identNodeParser = new FromToken<>(IdentifierNode::new);

    private static final Require<IdentifierNode> returnTypeParser = new Require<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition return type(s) must be " +
            "identifiers");
    private static final Compose<Node, IdentifierNode, IdentifierNode, TypeExpressionNode> parameterTypeParser = new Compose<>(
            new RequireWithTerminator<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type", TokenType.COLON),
            new Require<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type"),
            TypeExpressionNode::new);
    private static final Compose<Node, NodeList, NodeList, FunctionSignatureNode> functionSignatureParser =
            new Compose<>(
                    new Sequence<>(parameterTypeParser, (ParseContext<Node> pc) -> pc.lexer.consumeIf(TokenType.ARROW)),
                    new Sequence<>(returnTypeParser, (ParseContext<Node> pc) -> pc.lexer.peekIs(TokenType.LBRACE)),
                    FunctionSignatureNode::new);
    private static BiFunction<Node, Node, Node> f;
    private static final Compose<Node, FunctionSignatureNode, ExpressionNode, FunctionDeclarationNode> functionDeclarationNodeParser =
            new Compose<>(
                    functionSignatureParser, new Require<>(0, ExpressionNode.class, "A function implementation must be an expression"),
                    FunctionDeclarationNode::new);

    private static final Require<ExpressionNode> expressionParser = new Require<>(0, ExpressionNode.class, "All elements of an expression list enclosed by { } must be an expression");
    private static final Sequence<Node, ExpressionNode> expressionListParser =
            new Sequence<>(expressionParser, (ParseContext<Node> pc) -> pc.lexer.consumeIf(TokenType.RBRACE));
    private static final Convert<Node, NodeList, ExpressionListNode> leftBraceNodeParser = new
            Convert<>(expressionListParser, ExpressionListNode::new);


    // Infix parsers


    private static final Compose<Node, ExpressionNode, ExpressionNode, PlusNode> parseAddNode = new Compose<>(
            PLUS_MINUS.getLeft(),
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to plus"),
            new Require<>(PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to plus"),
            PlusNode::new);

    private static final Convert<Node, ExpressionNode, PostIncrementNode> postIncrementParser = new Convert<>(POST_INCREMENT.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for post-increment"), PostIncrementNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, MinusNode> subtractNodeParser = new Compose<>(PLUS_MINUS.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for lhs argument to minus"), new Require<>(PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to minus"),
            MinusNode::new);

    private static final Convert<Node, ExpressionNode, PostDecrementNode> postDecrementNodeParser = new Convert<>(POST_DECREMENT.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for post-decrement"), PostDecrementNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, MultiplyNode> multiplyNodeParser = new Compose<>(MULT_DIV.getLeft(), new RequireNode<>(ExpressionNode.class,
            "Expected an expression for lhs argument to multiply"), new Require<>(MULT_DIV.getRight(), ExpressionNode.class, "Expected an expression for rhs argument to multiply"),
            MultiplyNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, DivideNode> divideNodeParser = new Compose<>(MULT_DIV.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for lhs argument to divide"), new Require<>(MULT_DIV.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to divide"),
            DivideNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, AssignmentNode> assignmentNodeParser = new Compose<>(ASSIGNMENT.getLeft(), new RequireNode<>(ExpressionNode.class,
            "Must provide an expression for lhs argument to assignment"), new Require<>(ASSIGNMENT.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to " +
            "assignment"), AssignmentNode::new);

    private static final Compose<Node, Node, Node, CommaNode> commaNodeParser = new Compose<>(COMMA.getLeft(), new PassThrough<>(), new Require<>(COMMA.getRight(),
            Node.class, ""), CommaNode::new);

    private static final PassThrough<Node> rightParenNodeParser = new PassThrough<>(PARENS.getLeft());

    private static final Compose<Node, IdentifierNode, Node, FunctionCallNode> functionCallNodeParser = new Compose<>(
            PARENS.getLeft(),
            new RequireNode<>(IdentifierNode.class, "Function to be called must be an identifier node"),
            new Require<>(PARENS.getRight(), Node.class, "Error parsing function call arguments"),
            FunctionCallNode::new);

    private static final Constant<EmptyNode> eofNodeParser = new Constant<>(EOF.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> endOfLineParser = new Constant<>(EOL.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> colonNodeParser = new Constant<>(COLON.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> identifierNodeParser = new Constant<>(IDENTIFIER.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> arrowNodeParser = new Constant<>(ARROW.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> leftBraceInfixNodeParser = new Constant<>(BRACES.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> rightBraceInfixNodeParser = new Constant<>(BRACES.getLeft(), new EmptyNode());

    @Override
    public List<Node> parse() {
        List<Node> nodes = super.parse();
        return transform(nodes);
    }

    // TODO return an AbstractSyntaxTree that has function/type definitions
    private List<Node> transform(List<Node> nodes) {
        List<Node> transformed = new ArrayList<>();
        for (Node node : nodes) {
            transformed.add(node);
        }
        return transformed;
    }

    static class EmptyNode extends Node {
        @Override
        public String toString() {
            return "";
        }
    }

    static class ExpressionNode extends Node {
    }

    public static class LiteralNode extends ExpressionNode {
        private final String value;

        LiteralNode(final Token token) {
            this(token.getChars());
        }

        LiteralNode(final String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class UnaryOpNode extends ExpressionNode {
        private final ExpressionNode expr;
        private final String op;

        UnaryOpNode(final ExpressionNode expr, String op) {
            this.expr = expr;
            this.op = op;
        }

        ExpressionNode getExpr() {
            return expr;
        }

        @Override
        public String toString() {
            return op + "(" + expr.toString() + ")";
        }
    }

    static class NegativeSignedNode extends UnaryOpNode {
        NegativeSignedNode(final ExpressionNode expr) {
            super(expr, "-");
        }
    }

    static class PositiveSignedNode extends UnaryOpNode {
        PositiveSignedNode(final ExpressionNode expr) {
            super(expr, "+");
        }
    }

    static class PreIncrementNode extends UnaryOpNode {
        PreIncrementNode(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PreDecrementNode extends UnaryOpNode {
        PreDecrementNode(final ExpressionNode expr) {
            super(expr, "--");
        }
    }

    static class PostIncrementNode extends UnaryOpNode {
        PostIncrementNode(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PostDecrementNode extends UnaryOpNode {
        PostDecrementNode(final ExpressionNode expr) {
            super(expr, "--");
        }
    }

    public static class BinaryOpNode extends ExpressionNode {
        private final ExpressionNode lhs;
        private final ExpressionNode rhs;
        private final String op;

        BinaryOpNode(ExpressionNode lhs, ExpressionNode rhs, String op) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.op = op;
        }

        ExpressionNode getLhs() {
            return lhs;
        }

        ExpressionNode getRhs() {
            return rhs;
        }

        @Override
        public String toString() {
            return "(" + op + " " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    static class PlusNode extends BinaryOpNode {
        PlusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "+");
        }
    }

    static class MinusNode extends BinaryOpNode {
        MinusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "-");
        }
    }

    static class MultiplyNode extends BinaryOpNode {
        MultiplyNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "*");
        }
    }

    static class DivideNode extends BinaryOpNode {
        DivideNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "/");
        }
    }

    static class AssignmentNode extends BinaryOpNode {
        AssignmentNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "=");
        }
    }

    public static class IdentifierNode extends ExpressionNode {
        private final String chars;

        IdentifierNode(final Token token) {
            this(token.getChars());
        }

        IdentifierNode(final String chars) {
            this.chars = chars;
        }

        String getChars() {
            return chars;
        }

        @Override
        public String toString() {
            return chars;
        }
    }

    public static class CommaNode extends ExpressionNode {
        private final Node left;
        private final Node right;

        CommaNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        Node getLeft() {
            return left;
        }

        Node getRight() {
            return right;
        }

        @Override
        public String toString() {
            return left.toString() + ", " + right.toString();
        }
    }

    static class ExpressionListNode extends ExpressionNode {
        private final List<ExpressionNode> list;

        ExpressionListNode(final NodeList<ExpressionNode> nodes) {
            this(nodes.getNodes());
        }

        ExpressionListNode(final List<ExpressionNode> list) {
            this.list = list;
        }

        List<ExpressionNode> getList() {
            return list;
        }

        @Override
        public String toString() {
            return "{ " + list.stream().map(Object::toString).collect(Collectors.joining(",")) + " }";
        }
    }

    static class FunctionDeclarationNode extends ExpressionNode {
        private final FunctionSignatureNode functionSignature;
        private final ExpressionNode body;

        FunctionDeclarationNode(final FunctionSignatureNode functionSignature, final ExpressionNode body) {
            this.functionSignature = functionSignature;
            this.body = body;
        }

        ExpressionNode getBody() {
            return body;
        }

        FunctionSignatureNode getFunctionSignature() {
            return functionSignature;
        }

        @Override
        public String toString() {
            return "fn " + functionSignature + " " + body;
        }
    }

    static class FunctionCallNode extends ExpressionNode {
        private final String name;
        private final Node arguments;

        FunctionCallNode(final IdentifierNode name, final Node arguments) {
            this(name.getChars(), arguments);
        }

        FunctionCallNode(final String name, final Node arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        String getName() {
            return name;
        }

        Node getArguments() {
            return arguments;
        }

        @Override
        public String toString() {
            return "(" + name + " (" + arguments + "))";
        }
    }

    static class TypeExpressionNode extends ExpressionNode {
        private final IdentifierNode target;
        private final IdentifierNode typeExpression;

        TypeExpressionNode(final IdentifierNode target, final IdentifierNode expression) {
            this.target = target;
            typeExpression = expression;
        }

        IdentifierNode getTarget() {
            return target;
        }

        IdentifierNode getTypeExpression() {
            return typeExpression;
        }

        @Override
        public String toString() {
            return target + ":" + typeExpression;
        }
    }

    static class FunctionSignatureNode extends ExpressionNode {
        private final List<TypeExpressionNode> parameterTypes;
        private final List<IdentifierNode> returnTypes;

        FunctionSignatureNode(final NodeList<TypeExpressionNode> parameterTypes, final NodeList<IdentifierNode> returnTypes) {
            this.parameterTypes = parameterTypes.getNodes();
            this.returnTypes = returnTypes.getNodes();
        }

        List<TypeExpressionNode> getParameterTypes() {
            return parameterTypes;
        }

        List<IdentifierNode> getReturnTypes() {
            return returnTypes;
        }

        @Override
        public String toString() {
            String params = parameterTypes.isEmpty() ? "->" : parameterTypes.stream().map(Object::toString).collect(Collectors.joining(" ")) + " ->";
            return params + (returnTypes.isEmpty() ? "" : " ") + returnTypes.stream().map(Object::toString).collect(Collectors.joining(" "));
        }
    }
}
