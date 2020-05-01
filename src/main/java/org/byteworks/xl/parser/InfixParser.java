package org.byteworks.xl.parser;

import org.byteworks.xl.lexer.Lexer;

public interface InfixParser {
    Node parse(Node node, Parser parser, Lexer lexer);
}
