package org.byteworks.xl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.lexer.Token;
import org.byteworks.xl.lexer.TokenType;
import org.byteworks.xl.parser.InfixParser;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.NodeList;
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.PrefixParser;
import org.byteworks.xl.parser.rule.Any;
import org.byteworks.xl.parser.rule.Compose;
import org.byteworks.xl.parser.rule.Constant;
import org.byteworks.xl.parser.rule.Convert;
import org.byteworks.xl.parser.rule.FromToken;
import org.byteworks.xl.parser.rule.PassThrough;
import org.byteworks.xl.parser.rule.Require;
import org.byteworks.xl.parser.rule.RequireNode;
import org.byteworks.xl.parser.rule.RequireWithTerminator;
import org.byteworks.xl.parser.rule.Sequence;

public class XLParser extends Parser {

    public XLParser(final Lexer lexer, final PrintStream debugStream) {
        super(lexer, debugStream);
    }

    public static XLParser createParser(Lexer lexer, PrintStream debugStream) {
        XLParser parser = new XLParser(lexer, debugStream);
        for (ParserRule rule : ParserRule.values()) {
            parser.registerParserExpressionRule(rule.tokenType, rule.precedencePair, rule.prefixParser, rule.infixParser);
        }
        parser.registerPrefixParserRule(TokenType.EOL, eolParser);
        parser.registerPrefixParserRule(TokenType.LPAREN, lparenParser);
        parser.registerPrefixParserRule(TokenType.NUMBER, numberNodeParser);
        parser.registerPrefixParserRule(TokenType.RPAREN, constantEmpty);
        parser.registerPrefixParserRule(TokenType.MINUS, minusNodeParser);
        parser.registerPrefixParserRule(TokenType.PLUS, plusNodeParser);
        parser.registerPrefixParserRule(TokenType.MINUSMINUS, preDecrementNodeParser);
        parser.registerPrefixParserRule(TokenType.PLUSPLUS, preIncrementNodeParser);
        parser.registerPrefixParserRule(TokenType.IDENTIFIER, identNodeParser);
        parser.registerPrefixParserRule(TokenType.FUNCTION_DEFINITION, functionDeclarationNodeParser);
        parser.registerPrefixParserRule(TokenType.LBRACE, leftBraceNodeParser);

        return parser;
    }

    private static class PrecedencePairs {
        static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
        static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> PARENS = new Pair<>(1, 0);
        static final Pair<Integer, Integer> BRACES = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> COMMA = new Pair<>(1, 2);
        static final Pair<Integer, Integer> ARROW = new Pair<>(1, 2);
        static final Pair<Integer, Integer> ASSIGNMENT = new Pair<>(3, 4);
        static final Pair<Integer, Integer> PLUS_MINUS = new Pair<>(5, 6);
        static final Pair<Integer, Integer> MULT_DIV = new Pair<>(7, 8);
        static final Pair<Integer, Integer> SIGNED = new Pair<>(null, 10);
        static final Pair<Integer, Integer> PRE_INCREMENT = new Pair<>(null, 11);
        static final Pair<Integer, Integer> PRE_DECREMENT = new Pair<>(null, 11);
        static final Pair<Integer, Integer> POST_INCREMENT = new Pair<>(11, null);
        static final Pair<Integer, Integer> POST_DECREMENT = new Pair<>(11, null);
        static final Pair<Integer, Integer> COLON = new Pair<>(11, 12);
        static final Pair<Integer, Integer> IDENTIFIER = new Pair<>(11, 12);
        static final Pair<Integer, Integer> NUMBER = new Pair<>(-1, 12);
    }

    private static final Constant<EmptyNode> constantEmpty = new Constant<>(new EmptyNode());

    private static final Any<Node> eolParser = new Any<>(PrecedencePairs.EOL.getRight());

    private static final FromToken<LiteralNode> numberNodeParser = new FromToken<>(LiteralNode::new);

    private static final Convert<ExpressionNode, NegativeSignedNode> minusNodeParser = new Convert<>(
            new Require<>(PrecedencePairs.SIGNED.getRight(), ExpressionNode.class, "Must provide an expression for negative-signed"), NegativeSignedNode::new
    );

    private static final Convert<ExpressionNode, PositiveSignedNode> plusNodeParser = new Convert<>(
            new Require<>(PrecedencePairs.SIGNED.getRight(), ExpressionNode.class, "Must provide an expression for positive-signed"),
            PositiveSignedNode::new
    );

    private static final Convert<ExpressionNode, PreDecrementNode> preDecrementNodeParser = new Convert<>(
            new Require<>(PrecedencePairs.PRE_DECREMENT.getRight(), ExpressionNode.class, "Must provide an expression for pre-decrement"),
            PreDecrementNode::new
    );

    private static final Convert<ExpressionNode, PreIncrementNode> preIncrementNodeParser = new Convert<>(
            new Require<>(PrecedencePairs.PRE_INCREMENT.getRight(), ExpressionNode.class, "Must provide an expression for pre-increment"),
            PreIncrementNode::new
    );

    private static final Any<Node> lparenParser = new Any<>(PrecedencePairs.PARENS.getRight());

    private static final FromToken<IdentifierNode> identNodeParser = new FromToken<>(IdentifierNode::new);

    private static final Require<IdentifierNode> returnTypeParser = new Require<>(
            PrecedencePairs.IDENTIFIER.getRight(),
            IdentifierNode.class, "Function definition return type(s) must be identifiers");
    private static final Compose<IdentifierNode, IdentifierNode, TypeExpressionNode> parameterTypeParser =
            new Compose<>(
                    new RequireWithTerminator<>(
                            PrecedencePairs.IDENTIFIER.getRight(),
                            IdentifierNode.class,
                            "Function definition type expression must be of the form identifier:type",
                            TokenType.COLON),
                    new Require<>(
                            PrecedencePairs.IDENTIFIER.getRight(),
                            IdentifierNode.class,
                            "Function definition type expression must be of the form identifier:type"),
                    TypeExpressionNode::new);
    private static final Compose<NodeList<TypeExpressionNode>, NodeList<IdentifierNode>, FunctionSignatureNode> functionSignatureParser =
            new Compose<>(
                    new Sequence<>(parameterTypeParser, (ParseContext pc) -> pc.lexer.consumeIf(TokenType.ARROW)),
                    new Sequence<>(returnTypeParser, (ParseContext pc) -> pc.lexer.peekIs(TokenType.LBRACE)),
                    FunctionSignatureNode::new);
    private static final Compose<FunctionSignatureNode, ExpressionNode, FunctionDeclarationNode> functionDeclarationNodeParser = new Compose<>(
            functionSignatureParser,
            new Require<>(0, ExpressionNode.class, "A function implementation must be an expression"),
            FunctionDeclarationNode::new);

    private static final Require<ExpressionNode> expressionParser = new Require<>(0, ExpressionNode.class,
            "All elements of an expression list enclosed by { } must be an expression");
    private static final Sequence<ExpressionNode> expressionListParser = new Sequence<>(expressionParser, (ParseContext pc) -> pc.lexer.consumeIf(TokenType.RBRACE));
    private static final Convert<NodeList<ExpressionNode>, ExpressionListNode> leftBraceNodeParser = new Convert<>(
            expressionListParser, ExpressionListNode::new
    );

    // Infix parsers


    private static final Compose<ExpressionNode, ExpressionNode, PlusNode> parseAddNode = new Compose<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to plus"),
            new Require<>(PrecedencePairs.PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to plus"),
            PlusNode::new
    );
    private static final InfixParser add = (parseContext) -> parseAddNode.apply(parseContext);

    private static final Convert<ExpressionNode, PostIncrementNode> postIncrementParser = new Convert<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for post-increment"),
            PostIncrementNode::new
    );
    private static final InfixParser postIncrement = (parseContext) -> postIncrementParser.apply(parseContext);

    private static final Compose<ExpressionNode, ExpressionNode, MinusNode> subtractNodeParser = new Compose<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to minus"),
            new Require<>(PrecedencePairs.PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to minus"),
            MinusNode::new
    );
    private static final InfixParser subtract = (parseContext) -> subtractNodeParser.apply(parseContext);

    private static final Convert<ExpressionNode, PostDecrementNode> postDecrementNodeParser = new Convert<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for post-decrement"),
            PostDecrementNode::new
    );
    private static final InfixParser postDecrement = (parseContext) -> postDecrementNodeParser.apply(parseContext);


    private static final Compose<ExpressionNode, ExpressionNode, MultiplyNode> multiplyNodeParser = new Compose<>(
            new RequireNode<>(ExpressionNode.class, "Expected an expression for lhs argument to multiply"),
            new Require<>(PrecedencePairs.MULT_DIV.getRight(), ExpressionNode.class, "Expected an expression for rhs argument to multiply"),
            MultiplyNode::new
    );
    private static final InfixParser multiply = (parseContext) -> multiplyNodeParser.apply(parseContext);

    private static final Compose<ExpressionNode, ExpressionNode, DivideNode> divideNodeParser = new Compose<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to divide"),
            new Require<>(PrecedencePairs.MULT_DIV.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to divide"),
            DivideNode::new
    );
    private static final InfixParser divide = (parseContext) -> divideNodeParser.apply(parseContext);

    private static final Compose<ExpressionNode, ExpressionNode, AssignmentNode> assignmentNodeParser = new Compose<>(
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to assignment"),
            new Require<>(PrecedencePairs.ASSIGNMENT.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to assignment"),
            AssignmentNode::new
    );
    private static final InfixParser assignment = (parseContext) -> assignmentNodeParser.apply(parseContext);

    private static final Compose<Node, Node, CommaNode> commaNodeParser = new Compose<>(
            new PassThrough<>(),
            new Require<>(PrecedencePairs.COMMA.getRight(), Node.class, ""),
            CommaNode::new
    );
    private static final InfixParser comma = (parseContext) -> commaNodeParser.apply(parseContext);

    private static final PassThrough<Node> rightParenNodeParser = new PassThrough<>();
    private static final InfixParser rightParen = (parseContext) -> rightParenNodeParser.apply(parseContext);

    private static final Compose<IdentifierNode, Node, FunctionCallNode> functionCallNodeParser = new Compose<>(
            new RequireNode<>(IdentifierNode.class, "Function to be called must be an identifier node"),
            new Require<>(PrecedencePairs.PARENS.getRight(), Node.class, "Error parsing function call arguments"),
            FunctionCallNode::new
    );
    private static final InfixParser functionCall = (parseContext) -> functionCallNodeParser.apply(parseContext);

    private static final InfixParser endOfLine = (parseContext) -> constantEmpty.apply(parseContext);

    private enum ParserRule {
        PLUS(TokenType.PLUS, PrecedencePairs.PLUS_MINUS, null, add),
        MINUS(TokenType.MINUS, PrecedencePairs.PLUS_MINUS, null, subtract),
        MULTIPLY(TokenType.MULTIPLY, PrecedencePairs.MULT_DIV, null, multiply),
        DIVIDE(TokenType.DIVIDE, PrecedencePairs.MULT_DIV, null, divide),
        ASSIGNMENT(TokenType.ASSIGNMENT, PrecedencePairs.ASSIGNMENT, null, assignment),
        PLUSPLUS(TokenType.PLUSPLUS, PrecedencePairs.POST_INCREMENT, null, postIncrement),
        MINUSMINUS(TokenType.MINUSMINUS, PrecedencePairs.POST_DECREMENT, null, postDecrement),
        COMMA(TokenType.COMMA, PrecedencePairs.COMMA, null, comma),
        ARROW(TokenType.ARROW, PrecedencePairs.ARROW, null, null),
        COLON(TokenType.COLON, PrecedencePairs.COLON, null, null),
        EOF(TokenType.EOF, PrecedencePairs.EOF, null, null),
        NUMBER(TokenType.NUMBER, PrecedencePairs.NUMBER, null, null),
        LPAREN(TokenType.LPAREN, PrecedencePairs.PARENS, null, functionCall),
        IDENTIFIER(TokenType.IDENTIFIER, PrecedencePairs.IDENTIFIER, null, null),
        EOL(TokenType.EOL, PrecedencePairs.EOL, null, endOfLine),
        FUNCTION_DEFINITION(TokenType.FUNCTION_DEFINITION, null, null, null),
        LBRACE(TokenType.LBRACE, PrecedencePairs.BRACES, null, null),
        RPAREN(TokenType.RPAREN, PrecedencePairs.PARENS, null, rightParen),
        RBRACE(TokenType.RBRACE, PrecedencePairs.BRACES, null, null);

        final TokenType tokenType;
        final Pair<Integer, Integer> precedencePair;
        final PrefixParser prefixParser;
        final InfixParser infixParser;

        ParserRule(final TokenType tokenType, final Pair<Integer, Integer> precedencePair, final PrefixParser prefixParser, final InfixParser infixParser) {
            this.tokenType = tokenType;
            this.precedencePair = precedencePair;
            this.prefixParser = prefixParser;
            this.infixParser = infixParser;
        }
    }

    @Override
    public List<Node> parse() {
        List<Node> nodes = super.parse();
        return transform(nodes);
    }

    // TODO return an AbstractSyntaxTree that has function/type definitions
    private List<Node> transform(List<Node> nodes) {
        List<Node> transformed = new ArrayList<>();
        for (Node node : nodes) {
            transformed.add(node);
        }
        return transformed;
    }

    static class EmptyNode extends Node {
        @Override
        public String toString() {
            return "";
        }
    }

    static class ExpressionNode extends Node {
    }

    public static class LiteralNode extends ExpressionNode {
        private final String value;

        LiteralNode(final Token token) {
            this(token.getChars());
        }

        LiteralNode(final String value) {
            this.value = value;
        }

        String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static class UnaryOpNode extends ExpressionNode {
        private final ExpressionNode expr;
        private final String op;

        UnaryOpNode(final ExpressionNode expr, String op) {
            this.expr = expr;
            this.op = op;
        }

        ExpressionNode getExpr() {
            return expr;
        }

        @Override
        public String toString() {
            return op + "(" + expr.toString() + ")";
        }
    }

    static class NegativeSignedNode extends UnaryOpNode {
        NegativeSignedNode(final ExpressionNode expr) {
            super(expr, "-");
        }
    }

    static class PositiveSignedNode extends UnaryOpNode {
        PositiveSignedNode(final ExpressionNode expr) {
            super(expr, "+");
        }
    }

    static class PreIncrementNode extends UnaryOpNode {
        PreIncrementNode(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PreDecrementNode extends UnaryOpNode {
        PreDecrementNode(final ExpressionNode expr) {
            super(expr, "--");
        }
    }

    static class PostIncrementNode extends UnaryOpNode {
        PostIncrementNode(final ExpressionNode expr) {
            super(expr, "++");
        }
    }

    static class PostDecrementNode extends UnaryOpNode {
        PostDecrementNode(final ExpressionNode expr) {
            super(expr, "--");
        }
    }

    public static class BinaryOpNode extends ExpressionNode {
        private final ExpressionNode lhs;
        private final ExpressionNode rhs;
        private final String op;

        BinaryOpNode(ExpressionNode lhs, ExpressionNode rhs, String op) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.op = op;
        }

        ExpressionNode getLhs() {
            return lhs;
        }

        ExpressionNode getRhs() {
            return rhs;
        }

        @Override
        public String toString() {
            return "(" + op + " " + lhs.toString() + " " + rhs.toString() + ")";
        }
    }

    static class PlusNode extends BinaryOpNode {
        PlusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "+");
        }
    }

    static class MinusNode extends BinaryOpNode {
        MinusNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "-");
        }
    }

    static class MultiplyNode extends BinaryOpNode {
        MultiplyNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "*");
        }
    }

    static class DivideNode extends BinaryOpNode {
        DivideNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "/");
        }
    }

    static class AssignmentNode extends BinaryOpNode {
        AssignmentNode(final ExpressionNode lhs, final ExpressionNode rhs) {
            super(lhs, rhs, "=");
        }
    }

    public static class IdentifierNode extends ExpressionNode {
        private final String chars;

        IdentifierNode(final Token token) {
            this(token.getChars());
        }

        IdentifierNode(final String chars) {
            this.chars = chars;
        }

        String getChars() {
            return chars;
        }

        @Override
        public String toString() {
            return chars;
        }
    }

    public static class CommaNode extends ExpressionNode {
        private final Node left;
        private final Node right;

        CommaNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        Node getLeft() {
            return left;
        }

        Node getRight() {
            return right;
        }

        @Override
        public String toString() {
            return left.toString() + ", " + right.toString();
        }
    }

    static class ExpressionListNode extends ExpressionNode {
        private final List<ExpressionNode> list;

        ExpressionListNode(final NodeList<ExpressionNode> nodes) {
            this(nodes.getNodes());
        }

        ExpressionListNode(final List<ExpressionNode> list) {
            this.list = list;
        }

        List<ExpressionNode> getList() {
            return list;
        }

        @Override
        public String toString() {
            return "{ " + list.stream().map(Object::toString).collect(Collectors.joining(",")) + " }";
        }
    }

    static class FunctionDeclarationNode extends ExpressionNode {
        private final FunctionSignatureNode functionSignature;
        private final ExpressionNode body;

        FunctionDeclarationNode(final FunctionSignatureNode functionSignature, final ExpressionNode body) {
            this.functionSignature = functionSignature;
            this.body = body;
        }

        ExpressionNode getBody() {
            return body;
        }

        FunctionSignatureNode getFunctionSignature() {
            return functionSignature;
        }

        @Override
        public String toString() {
            return "fn " + functionSignature + " " + body;
        }
    }

    static class FunctionCallNode extends ExpressionNode {
        private final String name;
        private final Node arguments;

        FunctionCallNode(final IdentifierNode name, final Node arguments) {
            this(name.getChars(), arguments);
        }

        FunctionCallNode(final String name, final Node arguments) {
            this.name = name;
            this.arguments = arguments;
        }

        String getName() {
            return name;
        }

        Node getArguments() {
            return arguments;
        }

        @Override
        public String toString() {
            return "(" + name + " (" + arguments + "))";
        }
    }

    static class TypeExpressionNode extends ExpressionNode {
        private final IdentifierNode target;
        private final IdentifierNode typeExpression;

        TypeExpressionNode(final IdentifierNode target, final IdentifierNode expression) {
            this.target = target;
            typeExpression = expression;
        }

        IdentifierNode getTarget() {
            return target;
        }

        IdentifierNode getTypeExpression() {
            return typeExpression;
        }

        @Override
        public String toString() {
            return target + ":" + typeExpression;
        }
    }

    static class FunctionSignatureNode extends ExpressionNode {
        private final List<TypeExpressionNode> parameterTypes;
        private final List<IdentifierNode> returnTypes;

        FunctionSignatureNode(final NodeList<TypeExpressionNode> parameterTypes, final NodeList<IdentifierNode> returnTypes) {
            this.parameterTypes = parameterTypes.getNodes();
            this.returnTypes = returnTypes.getNodes();
        }

        List<TypeExpressionNode> getParameterTypes() {
            return parameterTypes;
        }

        List<IdentifierNode> getReturnTypes() {
            return returnTypes;
        }

        @Override
        public String toString() {
            String params = parameterTypes.isEmpty() ? "->" : parameterTypes.stream().map(Object::toString).collect(Collectors.joining(" ")) + " ->";
            return params + (returnTypes.isEmpty() ? "" : " ") + returnTypes.stream().map(Object::toString).collect(Collectors.joining(" "));
        }
    }
}
