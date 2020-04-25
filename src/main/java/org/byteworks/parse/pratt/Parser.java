package org.byteworks.parse.pratt;

import java.util.ArrayList;
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

    public static class LiteralNode extends Node {
        private final String value;
        public LiteralNode(String value) { this.value = value; }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class PlusNode extends Node {
        private final List<Node> lhs;
        private final List<Node> rhs;
        public PlusNode(List<Node> lhs, List<Node> rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(+ " + Parser.toString(lhs) + " " + Parser.toString(rhs) + ")";
        }
    }

    public static class MinusNode extends Node {
        private final List<Node> lhs;
        private final List<Node> rhs;
        public MinusNode(List<Node> lhs, List<Node> rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(- " + Parser.toString(lhs) + " " + Parser.toString(rhs) + ")";
        }
    }

    public static class MultNode extends Node {
        private final List<Node> lhs;
        private final List<Node> rhs;
        public MultNode(List<Node> lhs, List<Node> rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(* " + Parser.toString(lhs) + " " + Parser.toString(rhs) + ")";
        }
    }


    public static class DivideNode extends Node {
        private final List<Node> lhs;
        private final List<Node> rhs;
        public DivideNode(List<Node> lhs, List<Node> rhs) { this.lhs = lhs; this.rhs = rhs; }

        @Override
        public String toString() {
            return "(/ " + Parser.toString(lhs) + " " + Parser.toString(rhs) + ")";
        }
    }

    public List<Node> parse(Lexer lexer) {
        return parse(lexer, 0);
    }

    private List<Node> parse(final Lexer lexer, final int precedence) {
        List<Node> ast = new ArrayList<>();
        Lexer.Token token = lexer.next();
        if (token instanceof Lexer.Number) {
            ast.add(new LiteralNode(token.getChars()));
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
            List<Node> rhs = parse(lexer, precedencePair.right);
            if (token instanceof Lexer.Plus) {
                ast = rebuild(this::buildPlus, ast, rhs);
            } else if (token instanceof Lexer.Minus) {
                ast = rebuild(this::buildMinus, ast, rhs);
            } else if (token instanceof Lexer.Mult) {
                ast = rebuild(this::buildMult, ast, rhs);
            } else if (token instanceof Lexer.Divide) {
                ast = rebuild(this::buildDivide, ast, rhs);
            } else {
                throw new IllegalArgumentException("Could not parse " + token.getClass().getSimpleName() + "[" + token.getChars() + "]");
            }
        }
        return ast;
    }

    private List<Node> rebuild(final BiFunction<List<Node>, List<Node>, Node> ctor, final List<Node> ast, final List<Node> rhs) {
        Node node = ctor.apply(ast, rhs);
        List<Node> newAst = new ArrayList<>();
        newAst.add(node);
        return newAst;
    }

    private PlusNode buildPlus(List<Node> lhs, List<Node> rhs) {
        return new PlusNode(lhs, rhs);
    }

    private MinusNode buildMinus(List<Node> lhs, List<Node> rhs) {
        return new MinusNode(lhs, rhs);
    }

    private MultNode buildMult(List<Node> lhs, List<Node> rhs) {
        return new MultNode(lhs, rhs);
    }

    private DivideNode buildDivide(List<Node> lhs, List<Node> rhs) {
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
