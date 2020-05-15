package org.byteworks.xl.parser.rule;

import java.util.function.Function;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Convert<T extends Node, R extends Node> extends PrecNodeParseRule<R> {
    private final NodeParseRule<T> parseRule;
    private final Function<T, R> converter;

    public Convert(int precedence, final NodeParseRule<T> parseRule, final Function<T, R> converter) {
        super(precedence);
        this.parseRule = parseRule;
        this.converter = converter;
    }

    public Convert(final NodeParseRule<T> parseRule, final Function<T, R> converter) {
        this(DEFAULT_PRECEDENCE(), parseRule, converter);
    }

    @Override
    public R apply(final ParseContext context) {
        return converter.apply(parseRule.apply(context));
    }

}
