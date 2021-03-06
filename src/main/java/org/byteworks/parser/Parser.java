package org.byteworks.parser;

import org.byteworks.parser.rule.NodeParseRule;
import org.byteworks.lexer.Lexer;
import org.byteworks.lexer.Token;
import org.byteworks.lexer.TokenType;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Parser<T> {
    private final Lexer lexer;
    private final PrintStream debugStream;
    private final ParseContext<T> parseContext;

    public static <T> T require(ParseContext<T> parseContext, int precedence, Class<T> clazz, String error) {
        T node = parseContext.parse(precedence);
        return require(node, clazz, error);
    }

    public static <T> T require(T node, Class<T> clazz, String error) {
        if (!(clazz.isAssignableFrom(node.getClass()))) {
            throw new IllegalStateException(error + " (got " + node.getClass().getSimpleName() + "='" + node + "')");
        }
        return node;
    }

    public static Token require(Lexer lexer, TokenType tokenType, String error) {
        Token token = lexer.peek();
        if (!(token.getType() == tokenType)) {
            throw new IllegalStateException(error + "(got " + token + ")");
        }
        return lexer.next();
    }

    private final Map<TokenType, NodeParseRule<? extends T, ? extends T>> prefixParseRules = new HashMap<>();
    private final Map<TokenType, NodeParseRule<? extends T, ? extends T>> infixParseRules = new HashMap<>();

    public Parser(Lexer lexer, PrintStream debugStream) {
        this.lexer = lexer;
        this.debugStream = debugStream;
        this.parseContext = new ParseContext<>(this, lexer, debugStream);
    }

    public void registerPrefixParserRule(TokenType tokenType, NodeParseRule<? extends T, ? extends T> rule) {
        prefixParseRules.put(tokenType, rule);
    }

    public void registerInfixParserRule(TokenType tokenType, NodeParseRule<? extends T, ? extends T> rule) {
        infixParseRules.put(tokenType, rule);
    }

    public List<T> parse() {
        List<T> nodes = new ArrayList<>();
        while(lexer.hasMoreTokens()) {
            nodes.add(parse(0));
        }
        return nodes;
    }

    public T parse(final int precedence) {
        Token token = parseContext.nextToken();
        T node = parseFirstNode(token);
        while (shouldParseInfix(precedence)) {
            token = parseContext.nextToken();
            NodeParseRule<? extends T, ? extends T> rule = infixParseRules.get(token.getType());
            node = parseContext.parseInfix((NodeParseRule<T, T>) rule);
        }
        return node;
    }

    private boolean shouldParseInfix(int precedence) {
        Token token = lexer.peek();
        final int infixPrecedence = precedence(token);
        return infixPrecedence >= precedence;
    }

    private T parseFirstNode(Token token) {
        NodeParseRule<? extends T, ? extends T> prefixParseRule = prefixParseRules.get(token.getType());
        if (prefixParseRule != null) {
            return parseContext.parsePrefix((NodeParseRule<T, T>) prefixParseRule);
        }
        throw new IllegalArgumentException("No prefix parser registered for token " + token);
    }

    private int precedence(Token token) {
        NodeParseRule<? extends T, ? extends T> rule = infixParseRules.get(token.getType());
        return rule.precedence();
    }

}
