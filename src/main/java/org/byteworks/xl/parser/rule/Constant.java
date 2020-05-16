package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.ParseContext;

public class Constant<T> extends NodeParseRule<T, T> {
    private final T constant;

    public Constant(int precedence, final T constant) {
        super(precedence);
        this.constant = constant;
    }

    public Constant(final T constant) {
        this(DEFAULT_PRECEDENCE(), constant);
    }

    @Override
    public T apply(final ParseContext<T> context) {
        return constant;
    }
}
