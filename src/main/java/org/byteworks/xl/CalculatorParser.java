package org.byteworks.xl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.InfixParser;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.PrefixParser;

public class CalculatorParser {

    static class PrecedencePairs {
        static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
        static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> PARENS = new Pair<>(-1, 0);
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
        ARROW(TokenType.ARROW, PrecedencePairs.ARROW, null, new ArrowInfixParser()),
        COLON(TokenType.COLON, PrecedencePairs.COLON, null, new ColonInfixParser()),
        EOF(TokenType.EOF, PrecedencePairs.EOF, new EofPrefixParser(), null),
        NUMBER(TokenType.NUMBER, null, new NumberPrefixParser(), null),
        LPAREN(TokenType.LPAREN, null, new LParenPrefixParser(), null),
        IDENTIFIER(TokenType.IDENTIFIER, null, new IdentifierPrefixParser(), null),
        EOL(TokenType.EOL, PrecedencePairs.EOL, new EndOfLinePrefixParser(), null),
        FUNCTION_DEFINITION(TokenType.FUNCTION_DEFINITION, null, new FunctionDefinitionPrefixParser(), null),
        LBRACE(TokenType.LBRACE, PrecedencePairs.BRACES, new LeftBracePrefixParser(), null),
        RPAREN(TokenType.RPAREN, PrecedencePairs.PARENS, null, null),
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

    private static class EmptyNode extends Node {
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

        public String getChars() {
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

        public CommaNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        @Override
        public String toString() {
            return left.toString() + ", " + right.toString();
        }
    }

    public static class ProducesNode extends ExpressionNode {
        private final Node left;
        private final Node right;

        public ProducesNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        public Node getLeft() {
            return left;
        }

        public Node getRight() {
            return right;
        }

        @Override
        public String toString() {
            return "(" + left + " -> " + right + ")";
        }
    }

    static class ExpressionListNode extends ExpressionNode {
        private final List<ExpressionNode> list;

        ExpressionListNode(final List<ExpressionNode> list) {
            this.list = list;
        }

        public List<ExpressionNode> getList() {
            return list;
        }

        @Override
        public String toString() {
            return "{ " + list.stream().map(Object::toString).collect(Collectors.joining(",")) + " }";
        }
    }

    static class FunctionDeclarationNode extends ExpressionNode {
        private final ProducesNode typeSignature;
        private final ExpressionNode body;

        FunctionDeclarationNode(final ProducesNode typeSignature, final ExpressionNode body) {
            this.typeSignature = typeSignature;
            this.body = body;
        }

        public ProducesNode getTypeSignature() {
            return typeSignature;
        }

        public ExpressionNode getBody() {
            return body;
        }

        @Override
        public String toString() {
            return "fn" + typeSignature + " " + body;
        }
    }

    static class FunctionCallNode extends ExpressionNode {
        private final String chars;
        private final Node arguments;

        FunctionCallNode(final String chars, final Node arguments) {
            this.chars = chars;
            this.arguments = arguments;
        }

        public String getChars() {
            return chars;
        }

        public Node getArguments() {
            return arguments;
        }

        @Override
        public String toString() {
            return "(" + chars + " (" + arguments + "))";
        }
    }

    static class TypeExpressionNode extends ExpressionNode {
        private final IdentifierNode target;
        private final IdentifierNode typeExpression;

        TypeExpressionNode(final IdentifierNode target, final IdentifierNode expression) {
            this.target = target;
            typeExpression = expression;
        }

        public IdentifierNode getTarget() {
            return target;
        }

        public IdentifierNode getTypeExpression() {
            return typeExpression;
        }

        @Override
        public String toString() {
            return target + ":" + typeExpression;
        }
    }

    static class EofPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, Lexer lexer) {
            return new EmptyNode();
        }
    }

    static class EndOfLinePrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            return parser.parse(lexer, PrecedencePairs.EOL.getRight());
        }
    }

    static class NumberPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, Lexer lexer) {
            return new LiteralNode(token.getChars());
        }
    }

    static class MinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for negative-signed");
            }
            return new NegativeSignedNode((ExpressionNode) expr);
        }
    }

    static class PlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for positive-signed");
            }
            return new PositiveSignedNode((ExpressionNode) expr);
        }
    }

    static class MinusMinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PRE_DECREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-decrement");
            }
            return new PreDecrementNode((ExpressionNode) expr);
        }
    }

    static class PlusPlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PRE_INCREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-increment");
            }
            return new PreIncrementNode((ExpressionNode) expr);
        }
    }

    static class LParenPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PARENS.getRight());
            Token tok = lexer.next();
            if (!(tok.getType() == TokenType.RPAREN)) {
                throw new IllegalStateException("Expected a right parenthesis but got " + tok);
            }
            return expr;
        }
    }

    static class IdentifierPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            if (lexer.peek().getType() == TokenType.LPAREN) {
                Node arguments = parser.parse(lexer, PrecedencePairs.PARENS.getRight());
                return new FunctionCallNode(token.getChars(), arguments);
            }
            return new IdentifierNode(token.getChars());
        }
    }

    static class FunctionDefinitionPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            if (lexer.peek().getType() != TokenType.LPAREN) {
                throw new IllegalStateException("A function definition must begin with a type signature beginning with '('");
            }
            Node node = parser.parse(lexer, PrecedencePairs.PARENS.getRight());
            if (!(node instanceof ProducesNode)) {
                throw new IllegalStateException("A function definition must have a type signature of the form ([t1][,t2]*->t)");
            }
            ProducesNode typeSignature = (ProducesNode) node;
            Node body = parser.parse(lexer, 0);
            if (!(body instanceof ExpressionNode)) {
                throw new IllegalStateException("Function body must be an expression but was " + body);
            }
            return new FunctionDeclarationNode(typeSignature, (ExpressionNode) body);
        }
    }

    static class LeftBracePrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            List<ExpressionNode> nodes = new ArrayList<>();
            while (lexer.peek().getType() != TokenType.RBRACE) {
                Node node = parser.parse(lexer, 0);
                if (!(node instanceof ExpressionNode)) {
                    throw new IllegalStateException("All elements of an expression list enclosed by { } must be an expression, but '" + node + "' is not");
                }
                nodes.add((ExpressionNode) node);
            }
            lexer.next();
            return new ExpressionListNode(nodes);
        }
    }

    static class PlusInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.PLUS_MINUS.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to plus");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to plus");
            }
            return new PlusNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class PlusPlusInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for post-increment");
            }
            return new PostIncrementNode((ExpressionNode) node);
        }
    }

    static class MinusInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.PLUS_MINUS.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to minus");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to minus");
            }
            return new MinusNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class MinusMinusInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for post-decrement");
            }
            return new PostDecrementNode((ExpressionNode) node);
        }
    }

    static class MultiplyInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.MULT_DIV.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to multiply");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to multiply");
            }
            return new MultiplyNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class DivideInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.MULT_DIV.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to divide");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to divide");
            }
            return new DivideNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class AssignmentInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.ASSIGNMENT.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to assignment");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to assignment");
            }
            return new AssignmentNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class CommaInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node right = parser.parse(lexer, PrecedencePairs.COMMA.getRight());
            return new CommaNode(node, right);
        }
    }

    static class ArrowInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node right = parser.parse(lexer, PrecedencePairs.ARROW.getRight());
            return new ProducesNode(node, right);
        }
    }

    static class ColonInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final org.byteworks.xl.parser.Parser parser, final Lexer lexer) {
            if (!(node instanceof IdentifierNode)) {
                throw new IllegalStateException("Must provide an identifier as target of a type expression, but was " + node);
            }
            Node type = parser.parse(lexer, PrecedencePairs.COLON.getRight());
            if (!(type instanceof IdentifierNode)) {
                throw new IllegalStateException("Must provide an identifier as type of a type expression, but was " + node);
            }
            return new TypeExpressionNode((IdentifierNode) node, (IdentifierNode) type);
        }
    }

    public static Parser createParser() {
        Parser parser = new Parser();
        for (ParserRule rule : ParserRule.values()) {
            parser.registerParserRule(rule.tokenType, rule.precedencePair, rule.prefixParser, rule.infixParser);
        }
        return parser;
    }
}
