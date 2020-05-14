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

    private static class ParserExpressionRule {
        final Pair<Integer, Integer> precedencePair;
        final PrefixParser prefixParser;
        final InfixParser infixParser;

        public ParserExpressionRule(final Pair<Integer, Integer> precedencePair, final PrefixParser parser, final InfixParser infixParser) {
            this.precedencePair = precedencePair;
            this.prefixParser = parser;
            this.infixParser = infixParser;
        }
    }

    public static <T extends Node> T require(ParseContext parseContext, int precedence, Class clazz, String error) {
        Node node = parseContext.parse(precedence);
        return require(node, clazz, error);
    }

    public static <T extends Node> T require(Node node, Class clazz, String error) {
        if (!(clazz.isAssignableFrom(node.getClass()))) {
            throw new IllegalStateException(error + " (got " + node.getClass().getSimpleName() + "='" + node + "')");
        }
        return (T) node;
    }

    public static Token require(Lexer lexer, TokenType tokenType, String error) {
        Token token = lexer.peek();
        if (!(token.getType() == tokenType)) {
            throw new IllegalStateException(error + "(got " + token + ")");
        }
        return lexer.next();
    }

    private final Map<TokenType, ParserExpressionRule> parserRules = new HashMap<>();

    public Parser(Lexer lexer, PrintStream debugStream) {
        this.lexer = lexer;
        this.debugStream = debugStream;
        this.parseContext = new ParseContext(this, lexer, debugStream);
    }

    public void registerParserExpressionRule(TokenType tokenType, Pair<Integer, Integer> precedencePair, PrefixParser prefixParser, InfixParser infixParser) {
        if (parserRules.get(tokenType) != null) {
            throw new IllegalArgumentException("A parser rule has already been registered for " + tokenType);
        }
        parserRules.put(tokenType, new ParserExpressionRule(precedencePair, prefixParser, infixParser));
    }

    public List<Node> parse() {
        List<Node> nodes = new ArrayList<>();
        while(lexer.hasMoreTokens()) {
            nodes.add(parse(0));
        }
        return nodes;
    }

    public <T extends Node> T parse(final int precedence) {
        Token token = parseContext.nextToken();
        Node node = parseFirstNode(token);
        while (shouldParseInfix(precedence)) {
            token = parseContext.nextToken();
            InfixParser infixParser = infixParser(token);
            if (infixParser == null) {
                throw new IllegalStateException("Got no infix parser for token " + token.toString() + ", first node is " + node);
            }
            node = parseContext.parseInfix(infixParser);
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
        PrefixParser prefixParser = prefixParser(token);
        if (prefixParser != null) {
            return parseContext.parsePrefix(prefixParser);
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
