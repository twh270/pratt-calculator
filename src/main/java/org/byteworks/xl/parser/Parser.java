package org.byteworks.xl.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;

public class Parser {

    private final Map<TokenType, PrefixParser> prefixParsers;
    private final Map<TokenType, InfixParser> infixParsers;
    private final Map<TokenType, Pair<Integer, Integer>> tokenPrecedence;

    public Parser(Map<TokenType, PrefixParser> prefixParsers, Map<TokenType, InfixParser> infixParsers, Map<TokenType, Pair<Integer, Integer>> tokenPrecedence) {
        this.prefixParsers = prefixParsers;
        this.infixParsers = infixParsers;
        this.tokenPrecedence = tokenPrecedence;
    }


    public List<Node> parse(Lexer lexer) {
        List<Node> nodes = new ArrayList<>();
        while(lexer.hasMoreTokens()) {
            nodes.add(parse(lexer, 0));
            if (nodes.size() > 3) {
                throw new IllegalStateException("We don't want many lines yet -- blow up to avoid having to kill the VM");
            }
        }
        return nodes;
    }

    public Node parse(final Lexer lexer, final int precedence) {
        Token token = lexer.next();
        Node node = parseFirstNode(lexer, token);
        while (shouldParseInfix(lexer, precedence)) {
            token = lexer.next();
            node = infixParsers.get(token.getType()).parse(node, this, lexer);
        }
        return node;
    }

    private boolean shouldParseInfix(Lexer lexer, int precedence) {
        Token token = lexer.peek();
        final Pair<Integer, Integer> precedencePair = precedence(token);
        if (precedencePair == null) {
            throw new IllegalStateException("Got no precedence pair for parse infix, token = " + token);
        }
        return precedencePair.getLeft() >= precedence;
    }

    private Node parseFirstNode(final Lexer lexer, Token token) {
        if (prefixParsers.get(token.getType()) != null) {
            return prefixParsers.get(token.getType()).parse(token, this, lexer);
        }
        throw new IllegalArgumentException("Invalid token " + token);
    }

    private Pair<Integer, Integer> precedence(Token token) {
        return tokenPrecedence.get(token.getType());
    }

}
