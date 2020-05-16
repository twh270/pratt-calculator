package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.ParseContext;

public class PassThrough<T> extends NodeParseRule<T, T> {

    public PassThrough(int precedence) {
        super(precedence);
    }

    public PassThrough() {
        this(DEFAULT_PRECEDENCE());
    }

    @Override
    public T apply(final ParseContext<T> context) {
        return context.currentNode();
    }
}
