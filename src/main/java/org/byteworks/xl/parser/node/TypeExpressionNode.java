package org.byteworks.xl.parser.node;

public class TypeExpressionNode extends ExpressionNode {
    private final IdentifierNode target;
    private final IdentifierNode typeExpression;

    public TypeExpressionNode(final IdentifierNode target, final IdentifierNode expression) {
        this.target = target;
        typeExpression = expression;
    }

    public IdentifierNode getTarget() {
        return target;
    }

    public IdentifierNode getTypeExpression() {
        return typeExpression;
    }

    @Override
    public String toString() {
        return target + ":" + typeExpression;
    }
}
