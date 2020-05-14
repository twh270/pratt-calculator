package org.byteworks.xl.parser.rule;

import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.ParseContext;

public class Any<T extends Node> extends PrecNodeParseRule<T>  {

    public Any(final int precedence) {
        super(precedence);
    }

    @Override
    public T apply(final ParseContext context) {
        return (T) context.parse(precedence());
    }
}
