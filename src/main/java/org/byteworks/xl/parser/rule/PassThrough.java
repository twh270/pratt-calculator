package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class PassThrough<T extends Node> extends NodeParseRule<T> {

    public PassThrough(int precedence) {
        super(precedence);
    }

    public PassThrough() {
        this(DEFAULT_PRECEDENCE());
    }

    @Override
    public T apply(final ParseContext context) {
        return (T) context.currentNode();
    }
}
