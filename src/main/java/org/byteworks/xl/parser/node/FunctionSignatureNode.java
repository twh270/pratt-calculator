package org.byteworks.xl.parser.node;

import org.byteworks.parser.NodeList;

import java.util.List;
import java.util.stream.Collectors;

public class FunctionSignatureNode extends ExpressionNode {
    private final List<TypeExpressionNode> parameterTypes;
    private final List<IdentifierNode> returnTypes;

    public FunctionSignatureNode(final NodeList<TypeExpressionNode> parameterTypes, final NodeList<IdentifierNode> returnTypes) {
        this.parameterTypes = parameterTypes.getNodes();
        this.returnTypes = returnTypes.getNodes();
    }

    public List<TypeExpressionNode> getParameterTypes() {
        return parameterTypes;
    }

    public List<IdentifierNode> getReturnTypes() {
        return returnTypes;
    }

    @Override
    public String toString() {
        String params = parameterTypes.isEmpty() ? "->" : parameterTypes.stream().map(Object::toString).collect(Collectors.joining(" ")) + " ->";
        return params + (returnTypes.isEmpty() ? "" : " ") + returnTypes.stream().map(Object::toString).collect(Collectors.joining(" "));
    }
}
