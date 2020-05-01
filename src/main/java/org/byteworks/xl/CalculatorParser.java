package org.byteworks.xl;

import java.util.HashMap;
import java.util.Map;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.InfixParser;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.PrefixParser;

public class CalculatorParser {
    private static final Map<TokenType, PrefixParser> prefixParsers = new HashMap<>();
    private static final Map<TokenType, InfixParser> infixParsers = new HashMap<>();
    private static final Map<TokenType, Pair<Integer, Integer>> tokenPrecedence = new HashMap<>();

    static {
        infixParsers.put(TokenType.PLUS, new PlusInfixParser());
        infixParsers.put(TokenType.MINUS, new MinusInfixParser());
        infixParsers.put(TokenType.MULTIPLY, new MultInfixParser());
        infixParsers.put(TokenType.DIVIDE, new DivideInfixParser());
        infixParsers.put(TokenType.ASSIGNMENT, new AssignmentInfixParser());
        infixParsers.put(TokenType.PLUSPLUS, new PlusPlusInfixParser());
        infixParsers.put(TokenType.MINUSMINUS, new MinusMinusInfixParser());
        prefixParsers.put(TokenType.EOF, new EofPrefixParser());
        prefixParsers.put(TokenType.NUMBER, new NumberPrefixParser());
        prefixParsers.put(TokenType.MINUS, new MinusPrefixParser());
        prefixParsers.put(TokenType.PLUS, new PlusPrefixParser());
        prefixParsers.put(TokenType.LPAREN, new LParenPrefixParser());
        prefixParsers.put(TokenType.IDENTIFIER, new IdentifierPrefixParser());
        prefixParsers.put(TokenType.EOL, new EndOfLinePrefixParser());
        prefixParsers.put(TokenType.PLUSPLUS, new PlusPlusPrefixParser());
        prefixParsers.put(TokenType.MINUSMINUS, new MinusMinusPrefixParser());
        tokenPrecedence.put(TokenType.PLUS, PrecedencePairs.PLUS_MINUS);
        tokenPrecedence.put(TokenType.MINUS, PrecedencePairs.PLUS_MINUS);
        tokenPrecedence.put(TokenType.MULTIPLY, PrecedencePairs.MULT_DIV);
        tokenPrecedence.put(TokenType.DIVIDE, PrecedencePairs.MULT_DIV);
        tokenPrecedence.put(TokenType.EOF, PrecedencePairs.EOF);
        tokenPrecedence.put(TokenType.EOL, PrecedencePairs.EOL);
        tokenPrecedence.put(TokenType.RPAREN, PrecedencePairs.PARENS);
        tokenPrecedence.put(TokenType.ASSIGNMENT, PrecedencePairs.ASSIGNMENT);
        tokenPrecedence.put(TokenType.PLUSPLUS, PrecedencePairs.PRE_POST_INCREMENT);
        tokenPrecedence.put(TokenType.MINUSMINUS, PrecedencePairs.PRE_POST_DECREMENT);
    }

    static class PrecedencePairs {
        static final Pair<Integer,Integer> EOF = new Pair<>(-1, null);
        static final Pair<Integer,Integer> EOL = new Pair<>(-1, 0);
        static final Pair<Integer,Integer> PARENS = new Pair<>(-1, 0);
        static final Pair<Integer,Integer> PLUS_MINUS = new Pair<>(3, 4);
        static final Pair<Integer,Integer> MULT_DIV = new Pair<>(7, 8);
        static final Pair<Integer,Integer> SIGNED = new Pair<>(null, 10);
        static final Pair<Integer,Integer> ASSIGNMENT = new Pair<>(1, 2);
        static final Pair<Integer,Integer> PRE_INCREMENT = new Pair<>(null, 2);
        static final Pair<Integer,Integer> PRE_DECREMENT = new Pair<>(null, 2);
        static final Pair<Integer,Integer> PRE_POST_INCREMENT = new Pair<>(11, null);
        static final Pair<Integer,Integer> PRE_POST_DECREMENT = new Pair<>(11, null);
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

    static class NegativeSigned extends UnaryOpNode {
        NegativeSigned(final ExpressionNode expr) {
            super(expr, "-");
        }
    }

    static class PositiveSigned extends UnaryOpNode {
        PositiveSigned(final ExpressionNode expr) {
            super(expr, "+");
        }
    }

    static class PreIncrement extends UnaryOpNode {
        PreIncrement(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PreDecrement extends UnaryOpNode {
        PreDecrement(final ExpressionNode expr) {
            super(expr, "--");
        }
    }

    static class PostIncrement extends UnaryOpNode {
        PostIncrement(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PostDecrement extends UnaryOpNode {
        PostDecrement(final ExpressionNode expr) {
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

        Node getLhs() {
            return lhs;
        }

        Node getRhs() {
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

    static class MultNode extends BinaryOpNode {
        MultNode(final ExpressionNode lhs, final ExpressionNode rhs) {
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
            return new NegativeSigned((ExpressionNode) expr);
        }
    }

    static class MinusMinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PRE_DECREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-decrement");
            }
            return new PreDecrement((ExpressionNode) expr);
        }
    }

    static class PlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for positive-signed");
            }
            return new PositiveSigned((ExpressionNode) expr);
        }
    }

    static class PlusPlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PRE_INCREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-increment");
            }
            return new PreIncrement((ExpressionNode) expr);
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
            return new IdentifierNode(token.getChars());
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
            return new PostIncrement((ExpressionNode) node);
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
            return new PostDecrement((ExpressionNode) node);
        }
    }

    static class MultInfixParser implements InfixParser {

        @Override
        public Node parse(final Node node, final Parser parser, final Lexer lexer) {
            Node rhs = parser.parse(lexer, PrecedencePairs.MULT_DIV.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to multiply");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to multiply");
            }
            return new MultNode((ExpressionNode) node, (ExpressionNode) rhs);
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

    public static Parser createParser() {
        return new Parser(prefixParsers, infixParsers, tokenPrecedence);
    }
}
