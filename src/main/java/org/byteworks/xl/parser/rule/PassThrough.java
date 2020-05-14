package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class PassThrough<T extends Node> implements NodeParseRule<T> {

    @Override
    public T apply(final ParseContext context) {
        return (T) context.currentNode();
    }
}
