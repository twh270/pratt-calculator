package org.byteworks.xl.parser;

import java.io.PrintStream;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;

public class ParseContext {
    public final Parser parser;
    public final Lexer lexer;
    public final PrintStream debugStream;

    private Node currentNode;

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

    public Node parsePrefix(final PrefixParser parser, final Token token) {
        currentNode = parser.parse(this, token);
        return currentNode;
    }

    public Node parseInfix(final InfixParser parser, final Node node) {
        currentNode = parser.parse(this, node);
        return currentNode;
    }
}
