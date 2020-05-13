package org.byteworks.xl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.InfixParser;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.PrefixParser;

public class CalculatorParser extends Parser {

    public CalculatorParser(final Lexer lexer, final PrintStream debugStream) {
        super(lexer, debugStream);
    }

    public static CalculatorParser createParser(Lexer lexer, PrintStream debugStream) {
        CalculatorParser parser = new CalculatorParser(lexer, debugStream);
        for (ParserRule rule : ParserRule.values()) {
            parser.registerParserRule(rule.tokenType, rule.precedencePair, rule.prefixParser, rule.infixParser);
        }
        return parser;
    }

    private static class PrecedencePairs {
        static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
        static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> PARENS = new Pair<>(1, 0);
        static final Pair<Integer, Integer> BRACES = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> COMMA = new Pair<>(1, 2);
        static final Pair<Integer, Integer> ARROW = new Pair<>(1, 2);
        static final Pair<Integer, Integer> ASSIGNMENT = new Pair<>(3, 4);
        static final Pair<Integer, Integer> PLUS_MINUS = new Pair<>(5, 6);
        static final Pair<Integer, Integer> MULT_DIV = new Pair<>(7, 8);
        static final Pair<Integer, Integer> SIGNED = new Pair<>(null, 10);
        static final Pair<Integer, Integer> PRE_INCREMENT = new Pair<>(null, 11);
        static final Pair<Integer, Integer> PRE_DECREMENT = new Pair<>(null, 11);
        static final Pair<Integer, Integer> POST_INCREMENT = new Pair<>(11, null);
        static final Pair<Integer, Integer> POST_DECREMENT = new Pair<>(11, null);
        static final Pair<Integer, Integer> COLON = new Pair<>(11, 12);
        static final Pair<Integer, Integer> IDENTIFIER = new Pair<>(11, 12);
        static final Pair<Integer, Integer> NUMBER = new Pair<>(-1, 12);
    }

    private enum ParserRule {
        PLUS(TokenType.PLUS, PrecedencePairs.PLUS_MINUS, new PlusPrefixParser(), new PlusInfixParser()),
        MINUS(TokenType.MINUS, PrecedencePairs.PLUS_MINUS, new MinusPrefixParser(), new MinusInfixParser()),
        MULTIPLY(TokenType.MULTIPLY, PrecedencePairs.MULT_DIV, null, new MultiplyInfixParser()),
        DIVIDE(TokenType.DIVIDE, PrecedencePairs.MULT_DIV, null, new DivideInfixParser()),
        ASSIGNMENT(TokenType.ASSIGNMENT, PrecedencePairs.ASSIGNMENT, null, new AssignmentInfixParser()),
        PLUSPLUS(TokenType.PLUSPLUS, PrecedencePairs.POST_INCREMENT, new PlusPlusPrefixParser(), new PlusPlusInfixParser()),
        MINUSMINUS(TokenType.MINUSMINUS, PrecedencePairs.POST_DECREMENT, new MinusMinusPrefixParser(), new MinusMinusInfixParser()),
        COMMA(TokenType.COMMA, PrecedencePairs.COMMA, null, new CommaInfixParser()),
        ARROW(TokenType.ARROW, PrecedencePairs.ARROW, null, null),
        COLON(TokenType.COLON, PrecedencePairs.COLON, null, null),
        EOF(TokenType.EOF, PrecedencePairs.EOF, new EofPrefixParser(), null),
        NUMBER(TokenType.NUMBER, PrecedencePairs.NUMBER, new NumberPrefixParser(), null),
        LPAREN(TokenType.LPAREN, PrecedencePairs.PARENS, new LParenPrefixParser(), new LParenInfixParser()),
        IDENTIFIER(TokenType.IDENTIFIER, PrecedencePairs.IDENTIFIER, new IdentifierPrefixParser(), null),
        EOL(TokenType.EOL, PrecedencePairs.EOL, new EndOfLinePrefixParser(), null),
        FUNCTION_DEFINITION(TokenType.FUNCTION_DEFINITION, null, new FunctionDefinitionPrefixParser(), null),
        LBRACE(TokenType.LBRACE, PrecedencePairs.BRACES, new LeftBracePrefixParser(), null),
        RPAREN(TokenType.RPAREN, PrecedencePairs.PARENS, new RParenPrefixParser(), new RParenInfixParser()),
        RBRACE(TokenType.RBRACE, PrecedencePairs.BRACES, null, null);

        final TokenType tokenType;
        final Pair<Integer, Integer> precedencePair;
        final PrefixParser prefixParser;
        final InfixParser infixParser;

        ParserRule(final TokenType tokenType, final Pair<Integer, Integer> precedencePair, final PrefixParser prefixParser, final InfixParser infixParser) {
            this.tokenType = tokenType;
            this.precedencePair = precedencePair;
            this.prefixParser = prefixParser;
            this.infixParser = infixParser;
        }
    }

    private static <T extends Node> T require(ParseContext parseContext, int precedence, Class clazz, String error) {
        Node node = parseContext.parse(precedence);
        return require(node, clazz, error);
    }

    private static <T extends Node> T require(Node node, Class clazz, String error) {
        if (!(clazz.isAssignableFrom(node.getClass()))) {
            throw new IllegalStateException(error + " (got " + node.getClass().getSimpleName() + "='" + node + "')");
        }
        return (T) node;
    }

    private static Token require(Lexer lexer, TokenType tokenType, String error) {
        Token token = lexer.peek();
        if (!(token.getType() == tokenType)) {
            throw new IllegalStateException(error + "(got " + token + ")");
        }
        return lexer.next();
    }

    private static <T extends Node> List<T> parseNodeList(ParseContext parseContext, Class clazz, Function<ParseContext, T>elementParser, TokenType... terminators) {
        List<T> nodes = new ArrayList<>();
        while(!(parseContext.lexer.peekIs(terminators))) {
            T node = elementParser.apply(parseContext);
            require(node, clazz, "Got illegal node type parsing a list of node type " + clazz.getSimpleName());
            nodes.add(node);
        }
        return nodes;
    }

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

        LiteralNode(String value) {
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

        FunctionSignatureNode(final List<TypeExpressionNode> parameterTypes, final List<IdentifierNode> returnTypes) {
            this.parameterTypes = parameterTypes;
            this.returnTypes = returnTypes;
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

    static class EofPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return new EmptyNode();
        }
    }

    static class EndOfLinePrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return parseContext.parse(PrecedencePairs.EOL.getRight());
        }
    }

    static class NumberPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return new CalculatorParser.LiteralNode(token.getChars());
        }
    }

    static class MinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            ExpressionNode expr = require(parseContext, PrecedencePairs.SIGNED.getRight(), ExpressionNode.class, "Must provide an expression for negative-signed");
            return new NegativeSignedNode(expr);
        }
    }

    static class PlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            ExpressionNode expr = require(parseContext, PrecedencePairs.SIGNED.getRight(), ExpressionNode.class, "Must provide an expression for positive-signed");
            return new PositiveSignedNode(expr);
        }
    }

    static class MinusMinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            ExpressionNode expr = require(parseContext, PrecedencePairs.PRE_DECREMENT.getRight(), ExpressionNode.class, "Must provide an expression for pre-decrement");
            return new PreDecrementNode(expr);
        }
    }

    static class PlusPlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            ExpressionNode expr = require(parseContext, PrecedencePairs.PRE_INCREMENT.getRight(), ExpressionNode.class, "Must provide an expression for pre-increment");
            return new PreIncrementNode(expr);
        }
    }

    static class LParenPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return parseContext.parse(PrecedencePairs.PARENS.getRight());
        }
    }

    static class RParenPrefixParser implements PrefixParser {

        @Override
        public Node parse(final ParseContext parseContext, final Token token) {
            return new EmptyNode();
        }
    }

    static class IdentifierPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return new IdentifierNode(token.getChars());
        }
    }

    private abstract static class NodeParseRule<T extends Node> implements Function<ParseContext, T> {
        static class Require<T extends Node> extends NodeParseRule<T> {
            private final int precedence;
            private final Class clazz;
            private final String error;

            Require(final int precedence, final Class clazz, final String error) {
                this.precedence = precedence;
                this.clazz = clazz;
                this.error = error;
            }

            protected Class expectedClass() {
                return clazz;
            }

            @Override
            public T apply(final ParseContext parseContext) {
                return require(parseContext, precedence, clazz, error);
            }
        }

        static class RequireWithTerminator<T extends Node> extends Require<T> {
            private final TokenType terminator;

            RequireWithTerminator(final int precedence, final Class clazz, final String error, TokenType terminator) {
                super(precedence, clazz, error);
                this.terminator = terminator;
            }

            @Override
            public T apply(final ParseContext parseContext) {
                T node = super.apply(parseContext);
                require(parseContext.lexer, terminator, "Expecting " + expectedClass().getSimpleName() + " followed by " + terminator);
                return node;
            }
        }

        static class Compose<T extends Node, U extends Node, R extends Node> extends NodeParseRule<R> {
            private final NodeParseRule<T> left;
            private final NodeParseRule<U> right;
            private final BiFunction<T, U, R> composer;

            Compose(final NodeParseRule<T> left, final NodeParseRule<U> right, final BiFunction<T, U, R> composer) {
                this.left = left;
                this.right = right;
                this.composer = composer;
            }

            @Override
            public R apply(final ParseContext context) {
                T leftNode = left.apply(context);
                U rightNode = right.apply(context);
                return composer.apply(leftNode, rightNode);
            }
        }
    }

    static class FunctionDefinitionPrefixParser implements PrefixParser {
        private final NodeParseRule.Require<IdentifierNode> returnTypeParser = new NodeParseRule.Require<>(
                PrecedencePairs.IDENTIFIER.getRight(),
                IdentifierNode.class, "Function definition return type(s) must be identifiers");

        private TypeExpressionNode parseTypeExpression(ParseContext parseContext) {
            IdentifierNode name = require(parseContext, PrecedencePairs.IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type");
            require(parseContext.lexer, TokenType.COLON, "Function definition type expression must be of the form identifier:type");
            IdentifierNode type = require(parseContext, PrecedencePairs.IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type");
            return new TypeExpressionNode(name, type);
        }

        private IdentifierNode parseReturnType(ParseContext parseContext) {
            return require(parseContext, PrecedencePairs.IDENTIFIER.getRight(), IdentifierNode.class, "Function definition return type(s) must be identifiers");
        }

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            List<TypeExpressionNode> parameterTypes = parseNodeList(parseContext, TypeExpressionNode.class, this::parseTypeExpression, TokenType.ARROW);
            require(parseContext.lexer, TokenType.ARROW, "Expecting an arrow ('->') after parameter types in function definition");
            List<IdentifierNode> returnTypes = parseNodeList(parseContext, IdentifierNode.class, returnTypeParser, TokenType.LBRACE, TokenType.ASSIGNMENT);
            ExpressionNode body = require(parseContext, 0, ExpressionNode.class, "A function implementation must be an expression");
            return new FunctionDeclarationNode(new FunctionSignatureNode(parameterTypes, returnTypes), body);
        }
    }

    static class LeftBracePrefixParser implements PrefixParser {
        private ExpressionNode parseExpression(ParseContext parseContext) {
            return require(parseContext, 0, ExpressionNode.class, "All elements of an expression list enclosed by { } must be an expression");
        }

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            List<ExpressionNode> nodes = parseNodeList(parseContext, ExpressionNode.class, this::parseExpression, TokenType.RBRACE);
            parseContext.lexer.next();
            return new ExpressionListNode(nodes);
        }
    }

    static class PlusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode lhs = require(node, ExpressionNode.class, "Must provide an expression for lhs argument to plus");
            ExpressionNode rhs = require(parseContext, PrecedencePairs.PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to plus");
            return new PlusNode(lhs, rhs);
        }
    }

    static class PlusPlusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode expr = require(node, ExpressionNode.class, "Must provide an expression for post-increment");
            return new PostIncrementNode(expr);
        }
    }

    static class MinusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode lhs = require(node, ExpressionNode.class, "Must provide an expression for lhs argument to minus");
            ExpressionNode rhs = require(parseContext, PrecedencePairs.PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to minus");
            return new MinusNode(lhs, rhs);
        }
    }

    static class MinusMinusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode expr = require(node, ExpressionNode.class, "Must provide an expression for post-decrement");
            return new PostDecrementNode(expr);
        }
    }

    static class MultiplyInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode lhs = require(node, ExpressionNode.class, "Expected an expression for lhs argument to multiply");
            ExpressionNode rhs = require(parseContext, PrecedencePairs.MULT_DIV.getRight(), ExpressionNode.class, "Expected an expression for rhs argument to multiply");
            return new MultiplyNode(lhs, rhs);
        }
    }

    static class DivideInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode lhs = require(node, ExpressionNode.class, "Must provide an expression for lhs argument to divide");
            ExpressionNode rhs = require(parseContext, PrecedencePairs.MULT_DIV.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to divide");
            return new DivideNode(lhs, rhs);
        }
    }

    static class AssignmentInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            ExpressionNode lhs = require(node, ExpressionNode.class, "Must provide an expression for lhs argument to assignment");
            ExpressionNode rhs = require(parseContext, PrecedencePairs.ASSIGNMENT.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to assignment");
            return new AssignmentNode(lhs, rhs);
        }
    }

    static class CommaInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node right = parseContext.parse(PrecedencePairs.COMMA.getRight());
            return new CommaNode(node, right);
        }
    }

    static class RParenInfixParser implements InfixParser {

        @Override
        public Node parse(final ParseContext parseContext, final Node node) {
            return node;
        }
    }

    static class LParenInfixParser implements InfixParser {

        @Override
        public Node parse(final ParseContext parseContext, final Node node) {
            Node arguments = parseContext.parse(PrecedencePairs.PARENS.getRight());
            return new FunctionCallNode(node.toString(), arguments);
        }
    }

}
