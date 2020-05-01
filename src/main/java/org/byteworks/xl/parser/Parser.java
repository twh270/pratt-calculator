package org.byteworks.xl.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;

public class Parser {
    private static class ParserRule {
        final Pair<Integer, Integer> precedencePair;
        final PrefixParser prefixParser;
        final InfixParser infixParser;

        public ParserRule(final Pair<Integer, Integer> precedencePair, final PrefixParser parser, final InfixParser infixParser) {
            this.precedencePair = precedencePair;
            prefixParser = parser;
            this.infixParser = infixParser;
        }
    }

    private final Map<TokenType, ParserRule> parserRules = new HashMap<>();

    public Parser() {
    }

    public void registerParserRule(TokenType tokenType, Pair<Integer, Integer> precedencePair, PrefixParser prefixParser, InfixParser infixParser) {
        if (parserRules.get(tokenType) != null) {
            throw new IllegalArgumentException("A parser rule has already been registered for " + tokenType);
        }
        parserRules.put(tokenType, new ParserRule(precedencePair, prefixParser, infixParser));
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
            InfixParser infixParser = infixParser(token);
            if (infixParser == null) {
                throw new IllegalStateException("Got no infix parser for token " + token.toString());
            }
            node = infixParser.parse(node, this, lexer);
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
        if (prefixParser(token) != null) {
            return prefixParser(token).parse(token, this, lexer);
        }
        throw new IllegalArgumentException("Invalid token " + token);
    }

    private Pair<Integer, Integer> precedence(Token token) {
        return parserRules.get(token.getType()).precedencePair;
    }

    private PrefixParser prefixParser(Token token) {
        return parserRules.get(token.getType()).prefixParser;
    }

    private InfixParser infixParser(Token token) {
        return parserRules.get(token.getType()).infixParser;
    }
}
