package org.byteworks.xl.parser.node;

public class PreIncrementNode extends UnaryOpNode {
    public PreIncrementNode(final ExpressionNode expr) {
        super(expr, "++");
    }
}
