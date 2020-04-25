package org.byteworks.parse.pratt;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

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

        public UnaryOpNode(final Node expr) {
            this.expr = expr;
        }

        public Node getExpr() {
            return expr;
        }

        @Override
        public String toString() {
            return "-(" + expr.toString() + ")";
        }
    }

    public static class NegationNode extends UnaryOpNode {

        public NegationNode(final Node expr) {
            super(expr);
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
        static final Pair<Integer,Integer> NEGATION = new Pair<>(9, 10);
    }

    public List<Node> parse(Lexer lexer) {
        return Collections.singletonList(parse(lexer, 0));
    }

    private Node parse(final Lexer lexer, final int precedence) {
        Node node = null;
        Lexer.Token token = lexer.next();
        if (token instanceof Lexer.Eof) {
            return new EmptyNode();
        } else if (token instanceof Lexer.Number) {
            node = new LiteralNode(token.getChars());
        } else if (token instanceof Lexer.Minus) {
            Node expr = parse(lexer, PrecedencePairs.NEGATION.right);
            node = new NegationNode(expr);
        } else if (token instanceof Lexer.LParen) {
            Node expr = parse(lexer, PrecedencePairs.PARENS.right);
            token = lexer.next();
            node = expr;
        }
        while (true) {
            token = lexer.peek();
            Pair<Integer, Integer> precedencePair = precedence(token);
            if (precedencePair.left < precedence) {
                break;
            }
            lexer.next();
            Node rhs = parse(lexer, precedencePair.right);
            if (token instanceof Lexer.Plus) {
                node = rebuild(this::buildPlus, node, rhs);
            } else if (token instanceof Lexer.Minus) {
                node = rebuild(this::buildMinus, node, rhs);
            } else if (token instanceof Lexer.Mult) {
                node = rebuild(this::buildMult, node, rhs);
            } else if (token instanceof Lexer.Divide) {
                node = rebuild(this::buildDivide, node, rhs);
            } else {
                throw new IllegalArgumentException("Could not parse " + token.toString());
            }
        }
        return node;
    }

    private Node rebuild(final BiFunction<Node, Node, Node> ctor, final Node lhs, final Node rhs) {
        Node node = ctor.apply(lhs, rhs);
        return node;
    }

    private PlusNode buildPlus(Node lhs, Node rhs) {
        return new PlusNode(lhs, rhs);
    }

    private MinusNode buildMinus(Node lhs, Node rhs) {
        return new MinusNode(lhs, rhs);
    }

    private MultNode buildMult(Node lhs, Node rhs) {
        return new MultNode(lhs, rhs);
    }

    private DivideNode buildDivide(Node lhs, Node rhs) {
        return new DivideNode(lhs, rhs);
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
