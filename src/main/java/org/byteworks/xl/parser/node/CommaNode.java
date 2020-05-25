package org.byteworks.xl.parser.node;

import org.byteworks.parser.Node;

public class CommaNode extends ExpressionNode {
    private final Node left;
    private final Node right;

    public CommaNode(final Node left, final Node right) {
        this.left = left;
        this.right = right;
    }

    public Node getLeft() {
        return left;
    }

    public Node getRight() {
        return right;
    }

    @Override
    public String toString() {
        return left.toString() + ", " + right.toString();
    }
}
