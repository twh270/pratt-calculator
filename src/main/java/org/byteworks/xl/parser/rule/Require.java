package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;

public class Require<T> extends NodeParseRule<Node, T> {
    private final Class clazz;
    private final String error;

    public Require(final int precedence, final Class clazz, final String error) {
        super(precedence);
        this.clazz = clazz;
        this.error = error;
    }

    @Override
    public T apply(final ParseContext<Node> parseContext) {
        return (T) Parser.require(parseContext, precedence(), clazz, error);
    }
}
