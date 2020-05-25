package org.byteworks.xl.parser.node;

public class PreDecrementNode extends UnaryOpNode {
    public PreDecrementNode(final ExpressionNode expr) {
        super(expr, "--");
    }
}
