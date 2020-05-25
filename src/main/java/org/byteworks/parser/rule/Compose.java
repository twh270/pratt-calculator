package org.byteworks.parser.rule;

import java.util.function.BiFunction;

import org.byteworks.parser.ParseContext;

public class Compose<T, U, V, R> extends NodeParseRule<T, R> {
    private final NodeParseRule<T, U> left;
    private final NodeParseRule<T, V> right;
    private final BiFunction<U, V, R> composer;

    public Compose(final int precedence, final NodeParseRule<T, U> left, final NodeParseRule<T, V> right, final BiFunction<U, V, R> composer) {
        super(precedence);
        this.left = left;
        this.right = right;
        this.composer = composer;
    }

    public Compose(final NodeParseRule<T, U> left, final NodeParseRule<T, V> right, final BiFunction<U, V, R> composer) {
        this(DEFAULT_PRECEDENCE(), left, right, composer);
    }

    @Override
    public R apply(final ParseContext<T> context) {
        U leftNode = left.apply(context);
        V rightNode = right.apply(context);
        return composer.apply(leftNode, rightNode);
    }
}
