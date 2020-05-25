package org.byteworks.xl.parser.node;

import org.byteworks.lexer.Token;

public class LiteralNode extends ExpressionNode {
    private final String value;

    public LiteralNode(final Token token) {
        this(token.getChars());
    }

    LiteralNode(final String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
