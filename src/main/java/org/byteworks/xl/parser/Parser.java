package org.byteworks.xl.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;

public class Parser {
    private final Lexer lexer;
    private final PrintStream debugStream;
    private final ParseContext parseContext;

    private static class ParserRule {
        final Pair<Integer, Integer> precedencePair;
        final PrefixParser prefixParser;
        final InfixParser infixParser;

        public ParserRule(final Pair<Integer, Integer> precedencePair, final PrefixParser parser, final InfixParser infixParser) {
            this.precedencePair = precedencePair;
            this.prefixParser = parser;
            this.infixParser = infixParser;
        }
    }

    private final Map<TokenType, ParserRule> parserRules = new HashMap<>();

    public Parser(Lexer lexer, PrintStream debugStream) {
        this.lexer = lexer;
        this.debugStream = debugStream;
        this.parseContext = new ParseContext(this, lexer, debugStream);
    }

    public void registerParserRule(TokenType tokenType, Pair<Integer, Integer> precedencePair, PrefixParser prefixParser, InfixParser infixParser) {
        if (parserRules.get(tokenType) != null) {
            throw new IllegalArgumentException("A parser rule has already been registered for " + tokenType);
        }
        parserRules.put(tokenType, new ParserRule(precedencePair, prefixParser, infixParser));
    }

    public List<Node> parse() {
        List<Node> nodes = new ArrayList<>();
        while(lexer.hasMoreTokens()) {
            nodes.add(parse(0));
        }
        return nodes;
    }

    public <T extends Node> T parse(final int precedence) {
        Token token = parseContext.lexer.next();
        Node node = parseFirstNode(token);
        while (shouldParseInfix(precedence)) {
            token = parseContext.lexer.next();
            InfixParser infixParser = infixParser(token);
            if (infixParser == null) {
                throw new IllegalStateException("Got no infix parser for token " + token.toString() + ", first node is " + node);
            }
            node = infixParser.parse(parseContext, node);
        }
        return (T) node;
    }

    private boolean shouldParseInfix(int precedence) {
        Token token = lexer.peek();
        final Pair<Integer, Integer> precedencePair = precedence(token);
        if (precedencePair == null) {
            throw new IllegalStateException("Got no precedence pair for parse infix, token = " + token);
        }
        return precedencePair.getLeft() >= precedence;
    }

    private Node parseFirstNode(Token token) {
        if (prefixParser(token) != null) {
            return prefixParser(token).parse(parseContext, token);
        }
        throw new IllegalArgumentException("No prefix parser registered for token " + token);
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
