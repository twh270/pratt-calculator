package org.byteworks.xl.parser.node;

public class PlusNode extends BinaryOpNode {
    public PlusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
        super(lhs, rhs, "+");
    }
}
