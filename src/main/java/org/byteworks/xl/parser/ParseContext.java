package org.byteworks.xl.parser;

import java.io.PrintStream;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;

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

    public Node parsePrefix(final PrefixParser parser) {
        currentNode = parser.parse(this);
        return currentNode;
    }

    public Node parseInfix(final InfixParser parser) {
        currentNode = parser.parse(this);
        return currentNode;
    }

    public Token nextToken() {
        currentToken = lexer.next();
        return currentToken;
    }
}
