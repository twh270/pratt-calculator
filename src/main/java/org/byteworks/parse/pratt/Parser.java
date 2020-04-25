package org.byteworks.parse.pratt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser {
    private static class Pair<L, R> {
        public final L left;
        public final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }
    }

    public static class Node {
    }

    public static class EmptyNode extends Node {
    }

    public static class ExpressionNode extends Node {
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
        private final Node expr;
        private final String op;

        public UnaryOpNode(final Node expr, String op) {
            this.expr = expr;
            this.op = op;
        }

        public Node getExpr() {
            return expr;
        }

        @Override
        public String toString() {
            return op + "(" + expr.toString() + ")";
        }
    }

    public static class NegativeSigned extends UnaryOpNode {
        public NegativeSigned(final Node expr) {
            super(expr, "-");
        }
    }

    public static class PositiveSigned extends UnaryOpNode {
        public PositiveSigned(final Node expr) {
            super(expr, "+");
        }
    }

    public static class BinaryOpNode extends ExpressionNode {
        private final Node lhs;
        private final Node rhs;
        private final String op;

        public BinaryOpNode(Node lhs, Node rhs, String op) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.op = op;
        }

        public Node getLhs() {
            return lhs;
        }

        public Node getRhs() {
            return rhs;
        }

        @Override
        public String toString() {
            return "(" + op + " " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    public static class PlusNode extends BinaryOpNode {
        public PlusNode(final Node lhs, final Node rhs) {
            super(lhs, rhs, "+");
        }
    }

    public static class MinusNode extends BinaryOpNode {
        public MinusNode(final Node lhs, final Node rhs) {
            super(lhs, rhs, "-");
        }
    }

    public static class MultNode extends BinaryOpNode {
        public MultNode(final Node lhs, final Node rhs) {
            super(lhs, rhs, "*");
        }
    }

    public static class DivideNode extends BinaryOpNode {
        public DivideNode(final Node lhs, final Node rhs) {
            super(lhs, rhs, "/");
        }
    }

    static class PrecedencePairs {
        static final Pair<Integer,Integer> EOF = new Pair<>(-1, 0);
        static final Pair<Integer,Integer> PARENS = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> PLUS_MINUS = new Pair<>(3, 4);
        static final Pair<Integer,Integer> MULT_DIV = new Pair<>(7, 8);
        static final Pair<Integer,Integer> SIGNED = new Pair<>(9, 10);
    }

    public List<Node> parse(Lexer lexer) {
        return Collections.singletonList(parse(lexer, 0));
    }

    private Node parse(final Lexer lexer, final int precedence) {
        Lexer.Token token = lexer.next();
        Node node = parseFirstNode(lexer, token);
        while (parseInfix(lexer, precedence)) {
            token = lexer.next();
            Pair<Integer, Integer> precedencePair = precedence(token);
            Node rhs = parse(lexer, precedencePair.right);
            if (token instanceof Lexer.Plus) {
                node = new PlusNode(node, rhs);
            } else if (token instanceof Lexer.Minus) {
                node = new MinusNode(node, rhs);
            } else if (token instanceof Lexer.Mult) {
                node = new MultNode(node, rhs);
            } else if (token instanceof Lexer.Divide) {
                node = new DivideNode(node, rhs);
            } else {
                throw new IllegalArgumentException("Could not parse " + token.toString());
            }
        }
        return node;
    }

    private boolean parseInfix(Lexer lexer, int precedence) {
        Lexer.Token token = lexer.peek();
        final Pair<Integer, Integer> precedencePair = precedence(token);
        return precedencePair.left >= precedence;
    }

    interface PrefixParser {
        Node parse(Lexer.Token token, Parser parser, Lexer lexer);
    }

    class EofPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            return new EmptyNode();
        }
    }

    class NumberPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            return new LiteralNode(token.getChars());
        }
    }

    class MinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Lexer.Token token, final Parser parser, Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.right);
            return new NegativeSigned(expr);
        }
    }

    class PlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.SIGNED.right);
            return new PositiveSigned(expr);
        }
    }

    class LParenPrefixParser implements PrefixParser {

        @Override
        public Node parse(final Lexer.Token token, final Parser parser, final Lexer lexer) {
            Node expr = parser.parse(lexer, PrecedencePairs.PARENS.right);
            Lexer.Token tok = lexer.next();
            if (!(tok instanceof Lexer.RParen)) {
                throw new IllegalStateException("Expected a right parenthesis but got " + tok);
            }
            return expr;
        }
    }

    private Map<Lexer.TokenType, PrefixParser> prefixParsers = new HashMap<>();

    private Node parseFirstNode(final Lexer lexer, Lexer.Token token) {
        prefixParsers.put(Lexer.TokenType.EOF, new EofPrefixParser());
        prefixParsers.put(Lexer.TokenType.NUMBER, new NumberPrefixParser());
        prefixParsers.put(Lexer.TokenType.MINUS, new MinusPrefixParser());
        prefixParsers.put(Lexer.TokenType.PLUS, new PlusPrefixParser());
        prefixParsers.put(Lexer.TokenType.LPAREN, new LParenPrefixParser());
        if (prefixParsers.get(token.getType()) != null) {
            return prefixParsers.get(token.getType()).parse(token, this, lexer);
        }
        throw new IllegalArgumentException("Invalid token " + token);
    }

    private Pair<Integer, Integer> precedence(Lexer.Token token) {
        if (token instanceof Lexer.Plus || token instanceof Lexer.Minus) {
            return PrecedencePairs.PLUS_MINUS;
        } else if (token instanceof Lexer.Mult || token instanceof Lexer.Divide) {
            return PrecedencePairs.MULT_DIV;
        } else if (token instanceof Lexer.Eof) {
            return PrecedencePairs.EOF;
        } else if (token instanceof Lexer.RParen) {
            return PrecedencePairs.PARENS;
        }
        throw new IllegalStateException("Bad token " + token.getChars());
    }

}
