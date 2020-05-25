package org.byteworks.xl.parser.node;

public class FunctionDeclarationNode extends ExpressionNode {
    private final FunctionSignatureNode functionSignature;
    private final ExpressionNode body;

    public FunctionDeclarationNode(final FunctionSignatureNode functionSignature, final ExpressionNode body) {
        this.functionSignature = functionSignature;
        this.body = body;
    }

    public ExpressionNode getBody() {
        return body;
    }

    public FunctionSignatureNode getFunctionSignature() {
        return functionSignature;
    }

    @Override
    public String toString() {
        return "fn " + functionSignature + " " + body;
    }
}
