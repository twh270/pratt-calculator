package org.byteworks.xl.parser.node;

public class PositiveSignedNode extends UnaryOpNode {
    public PositiveSignedNode(final ExpressionNode expr) {
        super(expr, "+");
    }
}
