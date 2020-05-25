package org.byteworks.xl.parser.node;

public class DivideNode extends BinaryOpNode {
    public DivideNode(final ExpressionNode lhs, final ExpressionNode rhs) {
        super(lhs, rhs, "/");
    }
}
