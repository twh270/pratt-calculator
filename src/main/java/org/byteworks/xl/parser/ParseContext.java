package org.byteworks.xl.parser;

import org.byteworks.xl.lexer.Lexer;

import java.io.PrintStream;

public class ParseContext {
    public final Parser parser;
    public final Lexer lexer;
    public final PrintStream debugStream;

    public ParseContext(Parser parser, Lexer lexer, PrintStream debug) {
        this.parser = parser;
        this.lexer = lexer;
        this.debugStream = debug;
    }
}
