package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Constant<T extends Node> extends NodeParseRule<T> {
    private final T constant;

    public Constant(int precedence, final T constant) {
        super(precedence);
        this.constant = constant;
    }

    public Constant(final T constant) {
        this(DEFAULT_PRECEDENCE(), constant);
    }

    @Override
    public T apply(final ParseContext context) {
        return constant;
    }
}
