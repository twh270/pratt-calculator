package org.byteworks.parser.rule;

import java.util.function.Function;

import org.byteworks.parser.ParseContext;

public class Convert<T, U, R> extends NodeParseRule<T, R> {
    private final NodeParseRule<T, U> parseRule;
    private final Function<U, R> converter;

    public Convert(int precedence, final NodeParseRule<T, U> parseRule, final Function<U, R> converter) {
        super(precedence);
        this.parseRule = parseRule;
        this.converter = converter;
    }

    public Convert(final NodeParseRule<T, U> parseRule, final Function<U, R> converter) {
        this(DEFAULT_PRECEDENCE(), parseRule, converter);
    }

    @Override
    public R apply(final ParseContext<T> context) {
        return converter.apply(parseRule.apply(context));
    }

}
