package org.byteworks.xl.parser;

import java.io.PrintStream;

import org.byteworks.xl.lexer.Lexer;

public class ParseContext {
    public final Parser parser;
    public final Lexer lexer;
    public final PrintStream debugStream;

    public ParseContext(Parser parser, Lexer lexer, PrintStream debug) {
        this.parser = parser;
        this.lexer = lexer;
        this.debugStream = debug;
    }

    public Node parse(int precedence) {
        return parser.parse(precedence);
    }
}
