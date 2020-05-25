package org.byteworks.parser;

import java.io.PrintStream;

import org.byteworks.lexer.Lexer;
import org.byteworks.lexer.Token;
import org.byteworks.parser.rule.NodeParseRule;

public class ParseContext<T> {
    public final Parser<T> parser;
    public final Lexer lexer;
    public final PrintStream debugStream;

    private T currentNode;
    private Token currentToken;

    public ParseContext(Parser<T> parser, Lexer lexer, PrintStream debug) {
        this.parser = parser;
        this.lexer = lexer;
        this.debugStream = debug;
    }

    public T parse(int precedence) {
        return parser.parse(precedence);
    }

    public T currentNode() {
        return currentNode;
    }

    public Token currentToken() {
        return currentToken;
    }

    public T parsePrefix(final NodeParseRule<T, T> rule) {
        currentNode = rule.apply(this);
        return currentNode;
    }

    public T parseInfix(final NodeParseRule<T, T> rule) {
        currentNode = rule.apply(this);
        return currentNode;
    }

    public Token nextToken() {
        currentToken = lexer.next();
        return currentToken;
    }

}
