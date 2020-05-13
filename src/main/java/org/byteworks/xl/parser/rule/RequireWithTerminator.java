package org.byteworks.xl.parser.rule;

import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;

public class RequireWithTerminator<T extends Node> extends Require<T> {
    private final TokenType terminator;

    public RequireWithTerminator(final int precedence, final Class clazz, final String error, TokenType terminator) {
        super(precedence, clazz, error);
        this.terminator = terminator;
    }

    @Override
    public T apply(final ParseContext parseContext) {
        T node = super.apply(parseContext);
        Parser.require(parseContext.lexer, terminator, "Expecting " + expectedClass().getSimpleName() + " followed by " + terminator);
        return node;
    }
}
