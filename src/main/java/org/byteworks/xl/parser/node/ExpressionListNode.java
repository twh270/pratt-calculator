package org.byteworks.xl.parser.node;

import org.byteworks.parser.NodeList;

import java.util.List;
import java.util.stream.Collectors;

public class ExpressionListNode extends ExpressionNode {
    private final List<ExpressionNode> list;

    public ExpressionListNode(final NodeList<ExpressionNode> nodes) {
        this(nodes.getNodes());
    }

    ExpressionListNode(final List<ExpressionNode> list) {
        this.list = list;
    }

    public List<ExpressionNode> getList() {
        return list;
    }

    @Override
    public String toString() {
        return "{ " + list.stream().map(Object::toString).collect(Collectors.joining(",")) + " }";
    }
}
