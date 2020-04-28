package org.byteworks.parse.pratt;

import java.util.HashMap;
import java.util.Map;

public class CalculatorParser {
    private static final Map<Lexer.TokenType, Parser.PrefixParser> prefixParsers = new HashMap<>();
    private static final Map<Lexer.TokenType, Parser.InfixParser> infixParsers = new HashMap<>();
    private static final Map<Lexer.TokenType, Parser.Pair<Integer, Integer>> tokenPrecedence = new HashMap<>();

    static {
        infixParsers.put(Lexer.TokenType.PLUS, new PlusInfixParser());
        infixParsers.put(Lexer.TokenType.MINUS, new MinusInfixParser());
        infixParsers.put(Lexer.TokenType.MULTIPLY, new MultInfixParser());
        infixParsers.put(Lexer.TokenType.DIVIDE, new DivideInfixParser());
        infixParsers.put(Lexer.TokenType.ASSIGNMENT, new AssignmentInfixParser());
        infixParsers.put(Lexer.TokenType.PLUSPLUS, new PlusPlusInfixParser());
        infixParsers.put(Lexer.TokenType.MINUSMINUS, new MinusMinusInfixParser());
        prefixParsers.put(Lexer.TokenType.EOF, new EofPrefixParser());
        prefixParsers.put(Lexer.TokenType.NUMBER, new NumberPrefixParser());
        prefixParsers.put(Lexer.TokenType.MINUS, new MinusPrefixParser());
        prefixParsers.put(Lexer.TokenType.PLUS, new PlusPrefixParser());
        prefixParsers.put(Lexer.TokenType.LPAREN, new LParenPrefixParser());
        prefixParsers.put(Lexer.TokenType.IDENTIFIER, new IdentifierPrefixParser());
        prefixParsers.put(Lexer.TokenType.EOL, new EndOfLinePrefixParser());
        prefixParsers.put(Lexer.TokenType.PLUSPLUS, new PlusPlusPrefixParser());
        prefixParsers.put(Lexer.TokenType.MINUSMINUS, new MinusMinusPrefixParser());
        tokenPrecedence.put(Lexer.TokenType.PLUS, PrecedencePairs.PLUS_MINUS);
        tokenPrecedence.put(Lexer.TokenType.MINUS, PrecedencePairs.PLUS_MINUS);
        tokenPrecedence.put(Lexer.TokenType.MULTIPLY, PrecedencePairs.MULT_DIV);
        tokenPrecedence.put(Lexer.TokenType.DIVIDE, PrecedencePairs.MULT_DIV);
        tokenPrecedence.put(Lexer.TokenType.EOF, PrecedencePairs.EOF);
        tokenPrecedence.put(Lexer.TokenType.EOL, PrecedencePairs.EOL);
        tokenPrecedence.put(Lexer.TokenType.RPAREN, PrecedencePairs.PARENS);
        tokenPrecedence.put(Lexer.TokenType.ASSIGNMENT, PrecedencePairs.ASSIGNMENT);
        tokenPrecedence.put(Lexer.TokenType.PLUSPLUS, PrecedencePairs.PRE_POST_INCREMENT);
        tokenPrecedence.put(Lexer.TokenType.MINUSMINUS, PrecedencePairs.PRE_POST_DECREMENT);
    }

    static class PrecedencePairs {
        static final Parser.Pair<Integer,Integer> EOF = new Parser.Pair<>(-1, null);
        static final Parser.Pair<Integer,Integer> EOL = new Parser.Pair<>(-1, 0);
        static final Parser.Pair<Integer,Integer> PARENS = new Parser.Pair<>(-1, 0);
        static final Parser.Pair<Integer,Integer> PLUS_MINUS = new Parser.Pair<>(3, 4);
        static final Parser.Pair<Integer,Integer> MULT_DIV = new Parser.Pair<>(7, 8);
        static final Parser.Pair<Integer,Integer> SIGNED = new Parser.Pair<>(null, 10);
        static final Parser.Pair<Integer,Integer> ASSIGNMENT = new Parser.Pair<>(1, 2);
        static final Parser.Pair<Integer,Integer> PRE_INCREMENT = new Parser.Pair<>(null, 2);
        static final Parser.Pair<Integer,Integer> PRE_DECREMENT = new Parser.Pair<>(null, 2);
        static final Parser.Pair<Integer,Integer> PRE_POST_INCREMENT = new Parser.Pair<>(11, null);
        static final Parser.Pair<Integer,Integer> PRE_POST_DECREMENT = new Parser.Pair<>(11, null);
    }

    public static class EmptyNode extends Parser.Node {
    }

    public static class ExpressionNode extends Parser.Node {
    }

    public static class LiteralNode extends ExpressionNode {
        private final String value;

        public LiteralNode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class UnaryOpNode extends ExpressionNode {
        private final Parser.Node expr;
        private final String op;

        public UnaryOpNode(final Parser.Node expr, String op) {
            this.expr = expr;
            this.op = op;
        }

        public Parser.Node getExpr() {
            return expr;
        }

        @Override
        public String toString() {
            return op + "(" + expr.toString() + ")";
        }
    }

    public static class NegativeSigned extends UnaryOpNode {
        public NegativeSigned(final Parser.Node expr) {
            super(expr, "-");
        }
    }

    public static class PositiveSigned extends UnaryOpNode {
        public PositiveSigned(final Parser.Node expr) {
            super(expr, "+");
        }
    }

    public static class PreIncrement extends UnaryOpNode {
        public PreIncrement(final Parser.Node expr) {
            super(expr, "++");
        }
    }

    public static class PreDecrement extends UnaryOpNode {
        public PreDecrement(final Parser.Node expr) {
            super(expr, "--");
        }
    }

    public static class PostIncrement extends UnaryOpNode {
        public PostIncrement(final Parser.Node expr) {
            super(expr, "++");
        }
    }

    public static class PostDecrement extends UnaryOpNode {
        public PostDecrement(final Parser.Node expr) {
            super(expr, "--");
        }
    }

    public static class BinaryOpNode extends ExpressionNode {
        private final Parser.Node lhs;
        private final Parser.Node rhs;
        private final String op;

        public BinaryOpNode(Parser.Node lhs, Parser.Node rhs, String op) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.op = op;
        }

        public Parser.Node getLhs() {
            return lhs;
        }

        public Parser.Node getRhs() {
            return rhs;
        }

        @Override
        public String toString() {
            return "(" + op + " " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    static class PlusNode extends BinaryOpNode {
        public PlusNode(final Parser.Node lhs, final Parser.Node rhs) {
            super(lhs, rhs, "+");
        }
    }

    static class MinusNode extends BinaryOpNode {
        public MinusNode(final Parser.Node lhs, final Parser.Node rhs) {
            super(lhs, rhs, "-");
        }
    }

    static class MultNode extends BinaryOpNode {
        public MultNode(final Parser.Node lhs, final Parser.Node rhs) {
            super(lhs, rhs, "*");
        }
    }

    static class DivideNode extends BinaryOpNode {
        public DivideNode(final Parser.Node lhs, final Parser.Node rhs) {
            super(lhs, rhs, "/");
        }
    }

    static class AssignmentNode extends BinaryOpNode {
        public AssignmentNode(final Parser.Node lhs, final Parser.Node rhs) {
            super(lhs, rhs, "=");
        }
    }

    static class IdentifierNode extends ExpressionNode {
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

    static class EofPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            return new EmptyNode();
        }
    }

    static class EndOfLinePrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            return parser.parse(lexer, PrecedencePairs.EOL.getRight());
        }
    }

    static class NumberPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            return new LiteralNode(token.getChars());
        }
    }

    static class MinusPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            Parser.Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.getRight());
            return new NegativeSigned(expr);
        }
    }

    static class MinusMinusPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node expr = parser.parse(lexer, PrecedencePairs.PRE_DECREMENT.getRight());
            return new PreDecrement(expr);
        }
    }

    static class PlusPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.getRight());
            return new PositiveSigned(expr);
        }
    }

    static class PlusPlusPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node expr = parser.parse(lexer, PrecedencePairs.PRE_INCREMENT.getRight());
            return new PreIncrement(expr);
        }
    }

    static class LParenPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node expr = parser.parse(lexer, PrecedencePairs.PARENS.getRight());
            Lexer.Token tok = lexer.next();
            if (!(tok.getType() == Lexer.TokenType.RPAREN)) {
                throw new IllegalStateException("Expected a right parenthesis but got " + tok);
            }
            return expr;
        }
    }

    static class IdentifierPrefixParser implements Parser.PrefixParser {

        @Override
        public Parser.Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            return new IdentifierNode(token.getChars());
        }
    }

    static class PlusInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node rhs = parser.parse(lexer, PrecedencePairs.PLUS_MINUS.getRight());
            return new PlusNode(node, rhs);
        }
    }

    static class PlusPlusInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            return new PostIncrement(node);
        }
    }

    static class MinusInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node rhs = parser.parse(lexer, PrecedencePairs.PLUS_MINUS.getRight());
            return new MinusNode(node, rhs);
        }
    }

    static class MinusMinusInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            return new PostDecrement(node);
        }
    }

    static class MultInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node rhs = parser.parse(lexer, PrecedencePairs.MULT_DIV.getRight());
            return new MultNode(node, rhs);
        }
    }

    static class DivideInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node rhs = parser.parse(lexer, PrecedencePairs.MULT_DIV.getRight());
            return new DivideNode(node, rhs);
        }
    }

    static class AssignmentInfixParser implements Parser.InfixParser {

        @Override
        public Parser.Node parse(final Parser.Node node, final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Parser.Node rhs = parser.parse(lexer, PrecedencePairs.ASSIGNMENT.getRight());
            return new AssignmentNode(node, rhs);
        }
    }

    public static Parser createParser() {
        return new Parser(prefixParsers, infixParsers, tokenPrecedence);
    }
}
