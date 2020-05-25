package org.byteworks.xl.parser.node;

import org.byteworks.lexer.Token;

public class IdentifierNode extends ExpressionNode {
    private final String chars;

    public IdentifierNode(final Token token) {
        this(token.getChars());
    }

    IdentifierNode(final String chars) {
        this.chars = chars;
    }

    public String getChars() {
        return chars;
    }

    @Override
    public String toString() {
        return chars;
    }
}
