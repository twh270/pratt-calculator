package org.byteworks.xl.parser.rule;

import java.util.function.Function;

import org.byteworks.xl.parser.ParseContext;

public abstract class NodeParseRule<T, R> implements Function<ParseContext<T>, R> {
    private final int precedence;

    protected static int DEFAULT_PRECEDENCE() {
        return Integer.MAX_VALUE;
    }

    protected NodeParseRule() {
        this(DEFAULT_PRECEDENCE());
    }

    protected NodeParseRule(final int precedence) {
        this.precedence = precedence;
    }

    public int precedence() {
        return precedence;
    }
}
