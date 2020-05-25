package org.byteworks.xl.parser.node;

public class BinaryOpNode extends ExpressionNode {
    private final ExpressionNode lhs;
    private final ExpressionNode rhs;
    private final String op;

    BinaryOpNode(ExpressionNode lhs, ExpressionNode rhs, String op) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.op = op;
    }

    public ExpressionNode getLhs() {
        return lhs;
    }

    public ExpressionNode getRhs() {
        return rhs;
    }

    @Override
    public String toString() {
        return "(" + op + " " + lhs.toString() + " " + rhs.toString() + ")";
    }
}
