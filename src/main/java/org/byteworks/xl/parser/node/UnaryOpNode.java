package org.byteworks.xl.parser.node;

public class UnaryOpNode extends ExpressionNode {
    private final ExpressionNode expr;
    private final String op;

    UnaryOpNode(final ExpressionNode expr, String op) {
        this.expr = expr;
        this.op = op;
    }

    public ExpressionNode getExpr() {
        return expr;
    }

    @Override
    public String toString() {
        return op + "(" + expr.toString() + ")";
    }
}
