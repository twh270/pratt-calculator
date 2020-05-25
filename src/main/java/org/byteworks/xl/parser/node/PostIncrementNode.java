package org.byteworks.xl.parser.node;

public class PostIncrementNode extends UnaryOpNode {
    public PostIncrementNode(final ExpressionNode expr) {
        super(expr, "++");
    }
}
