package org.byteworks.xl.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.rule.NodeParseRule;

public class Parser {
    private final Lexer lexer;
    private final PrintStream debugStream;
    private final ParseContext parseContext;

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

    private final Map<TokenType, NodeParseRule<Node>> prefixParseRules = new HashMap<>();
    private final Map<TokenType, NodeParseRule<Node>> infixParseRules = new HashMap<>();

    public Parser(Lexer lexer, PrintStream debugStream) {
        this.lexer = lexer;
        this.debugStream = debugStream;
        this.parseContext = new ParseContext(this, lexer, debugStream);
    }

    public void registerPrefixParserRule(TokenType tokenType, NodeParseRule rule) {
        prefixParseRules.put(tokenType, rule);
    }

    public void registerInfixParserRule(TokenType tokenType, NodeParseRule rule) {
        infixParseRules.put(tokenType, rule);
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
            NodeParseRule<Node> rule = infixParseRules.get(token.getType());
            node = parseContext.parseInfix(rule);
        }
        return (T) node;
    }

    private boolean shouldParseInfix(int precedence) {
        Token token = lexer.peek();
        final int infixPrecedence = precedence(token);
        return infixPrecedence >= precedence;
    }

    private Node parseFirstNode(Token token) {
        NodeParseRule prefixParseRule = prefixParseRules.get(token.getType());
        if (prefixParseRule != null) {
            return parseContext.parsePrefix(prefixParseRule);
        }
        throw new IllegalArgumentException("No prefix parser registered for token " + token);
    }

    private int precedence(Token token) {
        NodeParseRule rule = infixParseRules.get(token.getType());
        return rule.precedence();
    }

}
