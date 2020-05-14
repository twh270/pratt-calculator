package org.byteworks.xl.parser.rule;

import static org.byteworks.xl.parser.Parser.require;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class RequireNode<T extends Node> implements NodeParseRule<T> {
    private final Class clazz;
    private final String error;

    public RequireNode(final Class clazz, final String error) {
        this.clazz = clazz;
        this.error = error;
    }

    @Override
    public T apply(final ParseContext context) {
        return require(context.currentNode(), clazz, error);
    }
}
