package org.byteworks.xl.parser.node;

public class NegativeSignedNode extends UnaryOpNode {
    public NegativeSignedNode(final ExpressionNode expr) {
        super(expr, "-");
    }
}
