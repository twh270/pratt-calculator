package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Constant<T extends Node> implements NodeParseRule<T> {
    private final T constant;

    public Constant(final T constant) {
        this.constant = constant;
    }

    @Override
    public T apply(final ParseContext context) {
        return constant;
    }
}
