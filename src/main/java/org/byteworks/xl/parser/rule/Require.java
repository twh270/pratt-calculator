package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;

public class Require<T extends Node> implements NodeParseRule<T> {
    private final int precedence;
    private final Class clazz;
    private final String error;

    public Require(final int precedence, final Class clazz, final String error) {
        this.precedence = precedence;
        this.clazz = clazz;
        this.error = error;
    }

    Class expectedClass() {
        return clazz;
    }

    @Override
    public T apply(final ParseContext parseContext) {
        return Parser.require(parseContext, precedence, clazz, error);
    }
}
