package org.byteworks.xl.parser.node;

import org.byteworks.parser.Node;

public class FunctionCallNode extends ExpressionNode {
    private final String name;
    private final Node arguments;

    public FunctionCallNode(final IdentifierNode name, final Node arguments) {
        this(name.getChars(), arguments);
    }

    FunctionCallNode(final String name, final Node arguments) {
        this.name = name;
        this.arguments = arguments;
    }

    public String getName() {
        return name;
    }

    public Node getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "(" + name + " (" + arguments + "))";
    }
}
