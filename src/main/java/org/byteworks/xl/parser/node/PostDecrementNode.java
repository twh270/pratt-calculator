package org.byteworks.xl.parser.node;

public class PostDecrementNode extends UnaryOpNode {
    public PostDecrementNode(final ExpressionNode expr) {
        super(expr, "--");
    }
}
