package org.byteworks.xl.parser.node;

public class AssignmentNode extends BinaryOpNode {
    public AssignmentNode(final ExpressionNode lhs, final ExpressionNode rhs) {
        super(lhs, rhs, "=");
    }
}
