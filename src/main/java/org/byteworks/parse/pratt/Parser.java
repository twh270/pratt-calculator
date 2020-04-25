package org.byteworks.parse.pratt;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Parser {

    private final Map<Lexer.TokenType, PrefixParser> prefixParsers;
    private final Map<Lexer.TokenType, InfixParser> infixParsers;
    private final Map<Lexer.TokenType, Pair<Integer, Integer>> tokenPrecedence;

    public Parser(Map<Lexer.TokenType, PrefixParser> prefixParsers, Map<Lexer.TokenType, InfixParser> infixParsers, Map<Lexer.TokenType, Pair<Integer, Integer>> tokenPrecedence) {
        this.prefixParsers = prefixParsers;
        this.infixParsers = infixParsers;
        this.tokenPrecedence = tokenPrecedence;
    }

    static class Pair<L, R> {
        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() {
            return left;
        }

        public R getRight() {
            return right;
        }
    }

    public static class Node {
    }


    public List<Node> parse(Lexer lexer) {
        return Collections.singletonList(parse(lexer, 0));
    }

    public Node parse(final Lexer lexer, final int precedence) {
        Lexer.Token token = lexer.next();
        Node node = parseFirstNode(lexer, token);
        while (shouldParseInfix(lexer, precedence)) {
            token = lexer.next();
            node = infixParsers.get(token.getType()).parse(node, token, this, lexer);
        }
        return node;
    }

    private boolean shouldParseInfix(Lexer lexer, int precedence) {
        Lexer.Token token = lexer.peek();
        final Pair<Integer, Integer> precedencePair = precedence(token);
        return precedencePair.left >= precedence;
    }

    interface InfixParser {
        Node parse(Node node, Lexer.Token token, Parser parser, Lexer lexer);
    }

    interface PrefixParser {
        Node parse(Lexer.Token token, Parser parser, Lexer lexer);
    }

    private Node parseFirstNode(final Lexer lexer, Lexer.Token token) {
        if (prefixParsers.get(token.getType()) != null) {
            return prefixParsers.get(token.getType()).parse(token, this, lexer);
        }
        throw new IllegalArgumentException("Invalid token " + token);
    }

    private Pair<Integer, Integer> precedence(Lexer.Token token) {
        return tokenPrecedence.get(token.getType());
    }

}
