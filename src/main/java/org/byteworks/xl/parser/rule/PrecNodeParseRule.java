package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;

public abstract class PrecNodeParseRule<T extends Node> implements NodeParseRule<T> {
    private final int precedence;

    protected static int DEFAULT_PRECEDENCE() {
        return Integer.MAX_VALUE;
    }

    protected PrecNodeParseRule() {
        this(DEFAULT_PRECEDENCE());
    }

    protected PrecNodeParseRule(final int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
