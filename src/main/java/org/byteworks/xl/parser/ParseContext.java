package org.byteworks.xl.parser;

import java.io.PrintStream;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.parser.rule.NodeParseRule;

public class ParseContext {
    public final Parser parser;
    public final Lexer lexer;
    public final PrintStream debugStream;

    private Node currentNode;
    private Token currentToken;

    public ParseContext(Parser parser, Lexer lexer, PrintStream debug) {
        this.parser = parser;
        this.lexer = lexer;
        this.debugStream = debug;
    }

    public Node parse(int precedence) {
        return parser.parse(precedence);
    }

    public Node currentNode() {
        return currentNode;
    }

    public Token currentToken() {
        return currentToken;
    }

    public <T extends Node> T parsePrefix(final NodeParseRule<T> rule) {
        currentNode = rule.apply(this);
        return (T) currentNode;
    }

    public <T extends Node> T parseInfix(final NodeParseRule<T> rule) {
        currentNode = rule.apply(this);
        return (T) currentNode;
    }

    public Token nextToken() {
        currentToken = lexer.next();
        return currentToken;
    }

}
