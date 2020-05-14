package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Any<T extends Node> implements NodeParseRule<T>  {
    private final int precedence;

    public Any(final int precedence) {
        this.precedence = precedence;
    }

    @Override
    public T apply(final ParseContext context) {
        return (T) context.parse(precedence);
    }
}
