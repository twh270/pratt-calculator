package org.byteworks.parser.rule;

import java.util.function.Function;

import org.byteworks.lexer.Token;
import org.byteworks.parser.ParseContext;

public class FromToken<T> extends NodeParseRule<T, T> {
    private final Function<Token, T> converter;

    public FromToken(final Function<Token, T> converter) {
        this.converter = converter;
    }

    @Override
    public T apply(final ParseContext<T> context) {
        return converter.apply(context.currentToken());
    }
}
