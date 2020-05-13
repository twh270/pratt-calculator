package org.byteworks.xl.parser.rule;

import java.util.function.BiFunction;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Compose<T extends Node, U extends Node, R extends Node> implements NodeParseRule<R> {
    private final NodeParseRule<T> left;
    private final NodeParseRule<U> right;
    private final BiFunction<T, U, R> composer;

    public Compose(final NodeParseRule<T> left, final NodeParseRule<U> right, final BiFunction<T, U, R> composer) {
        this.left = left;
        this.right = right;
        this.composer = composer;
    }

    @Override
    public R apply(final ParseContext context) {
        T leftNode = left.apply(context);
        U rightNode = right.apply(context);
        return composer.apply(leftNode, rightNode);
    }
}
