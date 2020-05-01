package org.byteworks.xl.parser;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;

public interface PrefixParser {
    Node parse(Token token, Parser parser, Lexer lexer);
}
