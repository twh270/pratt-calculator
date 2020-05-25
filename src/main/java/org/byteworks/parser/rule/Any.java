package org.byteworks.parser.rule;

import org.byteworks.parser.ParseContext;

public class Any<T> extends NodeParseRule<T, T> {

    public Any(final int precedence) {
        super(precedence);
    }

    @Override
    public T apply(final ParseContext<T> context) {
        return context.parse(precedence());
    }
}
