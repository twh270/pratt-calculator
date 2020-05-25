package org.byteworks.xl.parser.node;

public class MultiplyNode extends BinaryOpNode {
    public MultiplyNode(final ExpressionNode lhs, final ExpressionNode rhs) {
        super(lhs, rhs, "*");
    }
}
