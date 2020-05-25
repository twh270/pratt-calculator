package org.byteworks.xl.parser.node;

public class MinusNode extends BinaryOpNode {
    public MinusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
        super(lhs, rhs, "-");
    }
}
