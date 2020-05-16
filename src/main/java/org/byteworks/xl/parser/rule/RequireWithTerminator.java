package org.byteworks.xl.parser.rule;

import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;

public class RequireWithTerminator<T> extends NodeParseRule<Node, T> {
    private final TokenType terminator;
    private final Require<T> require;

    public RequireWithTerminator(final int precedence, final Class clazz, final String error, TokenType terminator) {
        super(precedence);
        this.require = new Require<T>(precedence, clazz, error);
        this.terminator = terminator;
    }

    @Override
    public T apply(final ParseContext<Node> parseContext) {
        T node = require.apply(parseContext);
        Parser.require(parseContext.lexer, terminator, "Expected terminator " + terminator);
        return node;
    }
}
