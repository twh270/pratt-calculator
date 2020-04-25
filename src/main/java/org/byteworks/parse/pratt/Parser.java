package org.byteworks.parse.pratt;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class Parser {
    private static class Pair<L,R> {
        public final L left;
        public final R right;
        public Pair(L left, R right) { this.left = left; this.right = right; }
    }

    public static class Node {
    }

    public static class ExpressionNode extends Node {

    }

    public static class LiteralNode extends ExpressionNode {
        private final String value;
        public LiteralNode(String value) { this.value = value; }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class PlusNode extends ExpressionNode {
        private final Node lhs;
        private final Node rhs;
        public PlusNode(Node lhs, Node rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(+ " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    public static class MinusNode extends ExpressionNode {
        private final Node lhs;
        private final Node rhs;
        public MinusNode(Node lhs, Node rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(- " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    public static class MultNode extends ExpressionNode {
        private final Node lhs;
        private final Node rhs;
        public MultNode(Node lhs, Node rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(* " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    public static class DivideNode extends ExpressionNode {
        private final Node lhs;
        private final Node rhs;
        public DivideNode(Node lhs, Node rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(/ " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    public List<Node> parse(Lexer lexer) {
        return Collections.singletonList(parse(lexer, 0));
    }

    /*
      0   == entry
      1,2 == +, -
      3,4 == *, /
     */
    private Node parse(final Lexer lexer, final int precedence) {
        Node node = null;
        Lexer.Token token = lexer.next();
        if (token instanceof Lexer.Number) {
            node = new LiteralNode(token.getChars());
        }
        while (true) {
            token = lexer.peek();
            if (token instanceof Lexer.Eof) {
                break;
            }
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
                throw new IllegalArgumentException("Could not parse " + token.getClass().getSimpleName() + "[" + token.getChars() + "]");
            }
        }
        return node;
    }

    private Node rebuild(final BiFunction<Node, Node, Node> ctor, final Node ast, final Node rhs) {
        Node node = ctor.apply(ast, rhs);
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
            return new Pair(1, 2);
        }
        if (token instanceof Lexer.Mult || token instanceof Lexer.Divide) {
            return new Pair(3, 4);
        }
        throw new IllegalStateException("Bad token " + token.getChars());
    }

    static String toString(List<Node> ast) {
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        return sb.toString();
    }
}
