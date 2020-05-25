package org.byteworks.xl.parser;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.byteworks.parser.Node;
import org.byteworks.lexer.Lexer;
import org.byteworks.lexer.TokenType;
import org.byteworks.parser.NodeList;
import org.byteworks.parser.Pair;
import org.byteworks.parser.ParseContext;
import org.byteworks.parser.Parser;
import org.byteworks.parser.rule.Any;
import org.byteworks.parser.rule.Compose;
import org.byteworks.parser.rule.Constant;
import org.byteworks.parser.rule.Convert;
import org.byteworks.parser.rule.FromToken;
import org.byteworks.parser.rule.PassThrough;
import org.byteworks.parser.rule.Require;
import org.byteworks.parser.rule.RequireNode;
import org.byteworks.parser.rule.RequireWithTerminator;
import org.byteworks.parser.rule.Sequence;
import org.byteworks.xl.parser.node.AssignmentNode;
import org.byteworks.xl.parser.node.CommaNode;
import org.byteworks.xl.parser.node.DivideNode;
import org.byteworks.xl.parser.node.EmptyNode;
import org.byteworks.xl.parser.node.ExpressionListNode;
import org.byteworks.xl.parser.node.ExpressionNode;
import org.byteworks.xl.parser.node.FunctionCallNode;
import org.byteworks.xl.parser.node.FunctionDeclarationNode;
import org.byteworks.xl.parser.node.FunctionSignatureNode;
import org.byteworks.xl.parser.node.IdentifierNode;
import org.byteworks.xl.parser.node.LiteralNode;
import org.byteworks.xl.parser.node.MinusNode;
import org.byteworks.xl.parser.node.MultiplyNode;
import org.byteworks.xl.parser.node.NegativeSignedNode;
import org.byteworks.xl.parser.node.PlusNode;
import org.byteworks.xl.parser.node.PositiveSignedNode;
import org.byteworks.xl.parser.node.PostDecrementNode;
import org.byteworks.xl.parser.node.PostIncrementNode;
import org.byteworks.xl.parser.node.PreDecrementNode;
import org.byteworks.xl.parser.node.PreIncrementNode;
import org.byteworks.xl.parser.node.TypeExpressionNode;

public class XLParser<T extends Node> extends Parser<T> {

    private static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
    private static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
    private static final Pair<Integer, Integer> PARENS = new Pair<>(1, 0);
    private static final Pair<Integer, Integer> BRACES = new Pair<>(-1, 0);
    private static final Pair<Integer, Integer> COMMA = new Pair<>(1, 2);
    private static final Pair<Integer, Integer> ARROW = new Pair<>(1, 2);
    private static final Pair<Integer, Integer> ASSIGNMENT = new Pair<>(3, 4);
    private static final Pair<Integer, Integer> PLUS_MINUS = new Pair<>(5, 6);
    private static final Pair<Integer, Integer> MULT_DIV = new Pair<>(7, 8);
    private static final Pair<Integer, Integer> SIGNED = new Pair<>(null, 10);
    private static final Pair<Integer, Integer> PRE_INCREMENT = new Pair<>(null, 11);
    private static final Pair<Integer, Integer> PRE_DECREMENT = new Pair<>(null, 11);
    private static final Pair<Integer, Integer> POST_INCREMENT = new Pair<>(11, null);
    private static final Pair<Integer, Integer> POST_DECREMENT = new Pair<>(11, null);
    private static final Pair<Integer, Integer> COLON = new Pair<>(11, 12);
    private static final Pair<Integer, Integer> IDENTIFIER = new Pair<>(11, 12);

    private static final Any<Node> eolParser = new Any<>(EOL.getRight());

    public XLParser(final Lexer lexer, final PrintStream debugStream) {
        super(lexer, debugStream);
    }

    public static XLParser<Node> createParser(Lexer lexer, PrintStream debugStream) {
        XLParser<Node> parser = new XLParser<>(lexer, debugStream);
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

        parser.registerInfixParserRule(TokenType.PLUS, parseAddNode);
        parser.registerInfixParserRule(TokenType.PLUSPLUS, postIncrementParser);
        parser.registerInfixParserRule(TokenType.MINUS, subtractNodeParser);
        parser.registerInfixParserRule(TokenType.MINUSMINUS, postDecrementNodeParser);
        parser.registerInfixParserRule(TokenType.MULTIPLY, multiplyNodeParser);
        parser.registerInfixParserRule(TokenType.DIVIDE, divideNodeParser);
        parser.registerInfixParserRule(TokenType.ASSIGNMENT, assignmentNodeParser);
        parser.registerInfixParserRule(TokenType.COMMA, commaNodeParser);
        parser.registerInfixParserRule(TokenType.RPAREN, rightParenNodeParser);
        parser.registerInfixParserRule(TokenType.LPAREN, functionCallNodeParser);
        parser.registerInfixParserRule(TokenType.EOL, endOfLineParser);
        parser.registerInfixParserRule(TokenType.EOF, eofNodeParser);
        parser.registerInfixParserRule(TokenType.COLON, colonNodeParser);
        parser.registerInfixParserRule(TokenType.IDENTIFIER, identifierNodeParser);
        parser.registerInfixParserRule(TokenType.ARROW, arrowNodeParser);
        parser.registerInfixParserRule(TokenType.LBRACE, leftBraceInfixNodeParser);
        parser.registerInfixParserRule(TokenType.RBRACE, rightBraceInfixNodeParser);
        return parser;
    }

    // Prefix parsers

    private static final Constant<EmptyNode> constantEmpty = new Constant<>(new EmptyNode());

    private static final FromToken<LiteralNode> numberNodeParser = new FromToken<>(LiteralNode::new);

    private static final Convert<Node, ExpressionNode, NegativeSignedNode> minusNodeParser = new Convert<>(
            new Require<>(SIGNED.getRight(), ExpressionNode.class, "Must provide an " +
            "expression for negative-signed"), NegativeSignedNode::new);

    private static final Convert<Node, ExpressionNode, PositiveSignedNode> plusNodeParser = new Convert<>(new Require<>(SIGNED.getRight(), ExpressionNode.class, "Must provide an " +
            "expression for positive-signed"), PositiveSignedNode::new);

    private static final Convert<Node, ExpressionNode, PreDecrementNode> preDecrementNodeParser = new Convert<>(new Require<>(PRE_DECREMENT.getRight(), ExpressionNode.class, "Must provide" +
            " an expression for pre-decrement"), PreDecrementNode::new);

    private static final Convert<Node, ExpressionNode, PreIncrementNode> preIncrementNodeParser = new Convert<>(new Require<>(PRE_INCREMENT.getRight(), ExpressionNode.class, "Must provide" +
            " an expression for pre-increment"), PreIncrementNode::new);

    private static final Any<Node> lparenParser = new Any<>(PARENS.getRight());

    private static final FromToken<IdentifierNode> identNodeParser = new FromToken<>(IdentifierNode::new);

    private static final Require<IdentifierNode> returnTypeParser = new Require<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition return type(s) must be " +
            "identifiers");
    private static final Compose<Node, IdentifierNode, IdentifierNode, TypeExpressionNode> parameterTypeParser = new Compose<>(
            new RequireWithTerminator<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type", TokenType.COLON),
            new Require<>(IDENTIFIER.getRight(), IdentifierNode.class, "Function definition type expression must be of the form identifier:type"),
            TypeExpressionNode::new);
    private static final Compose<Node, NodeList<TypeExpressionNode>, NodeList<IdentifierNode>, FunctionSignatureNode> functionSignatureParser =
            new Compose<>(
                    new Sequence<>(parameterTypeParser, (ParseContext<Node> pc) -> pc.lexer.consumeIf(TokenType.ARROW)),
                    new Sequence<>(returnTypeParser, (ParseContext<Node> pc) -> pc.lexer.peekIs(TokenType.LBRACE)),
                    FunctionSignatureNode::new);
    private static final Compose<Node, FunctionSignatureNode, ExpressionNode, FunctionDeclarationNode> functionDeclarationNodeParser =
            new Compose<>(
                    functionSignatureParser, new Require<>(0, ExpressionNode.class, "A function implementation must be an expression"),
                    FunctionDeclarationNode::new);

    private static final Require<ExpressionNode> expressionParser = new Require<>(0, ExpressionNode.class, "All elements of an expression list enclosed by { } must be an expression");
    private static final Sequence<Node, ExpressionNode> expressionListParser =
            new Sequence<>(expressionParser, (ParseContext<Node> pc) -> pc.lexer.consumeIf(TokenType.RBRACE));
    private static final Convert<Node, NodeList<ExpressionNode>, ExpressionListNode> leftBraceNodeParser = new
            Convert<>(expressionListParser, ExpressionListNode::new);


    // Infix parsers


    private static final Compose<Node, ExpressionNode, ExpressionNode, PlusNode> parseAddNode = new Compose<>(
            PLUS_MINUS.getLeft(),
            new RequireNode<>(ExpressionNode.class, "Must provide an expression for lhs argument to plus"),
            new Require<>(PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to plus"),
            PlusNode::new);

    private static final Convert<Node, ExpressionNode, PostIncrementNode> postIncrementParser = new Convert<>(POST_INCREMENT.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for post-increment"), PostIncrementNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, MinusNode> subtractNodeParser = new Compose<>(PLUS_MINUS.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for lhs argument to minus"), new Require<>(PLUS_MINUS.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to minus"),
            MinusNode::new);

    private static final Convert<Node, ExpressionNode, PostDecrementNode> postDecrementNodeParser = new Convert<>(POST_DECREMENT.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for post-decrement"), PostDecrementNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, MultiplyNode> multiplyNodeParser = new Compose<>(MULT_DIV.getLeft(), new RequireNode<>(ExpressionNode.class,
            "Expected an expression for lhs argument to multiply"), new Require<>(MULT_DIV.getRight(), ExpressionNode.class, "Expected an expression for rhs argument to multiply"),
            MultiplyNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, DivideNode> divideNodeParser = new Compose<>(MULT_DIV.getLeft(), new RequireNode<>(ExpressionNode.class, "Must " +
            "provide an expression for lhs argument to divide"), new Require<>(MULT_DIV.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to divide"),
            DivideNode::new);

    private static final Compose<Node, ExpressionNode, ExpressionNode, AssignmentNode> assignmentNodeParser = new Compose<>(ASSIGNMENT.getLeft(), new RequireNode<>(ExpressionNode.class,
            "Must provide an expression for lhs argument to assignment"), new Require<>(ASSIGNMENT.getRight(), ExpressionNode.class, "Must provide an expression for rhs argument to " +
            "assignment"), AssignmentNode::new);

    private static final Compose<Node, Node, Node, CommaNode> commaNodeParser = new Compose<>(COMMA.getLeft(), new PassThrough<>(), new Require<>(COMMA.getRight(),
            Node.class, ""), CommaNode::new);

    private static final PassThrough<Node> rightParenNodeParser = new PassThrough<>(PARENS.getLeft());

    private static final Compose<Node, IdentifierNode, Node, FunctionCallNode> functionCallNodeParser = new Compose<>(
            PARENS.getLeft(),
            new RequireNode<>(IdentifierNode.class, "Function to be called must be an identifier node"),
            new Require<>(PARENS.getRight(), Node.class, "Error parsing function call arguments"),
            FunctionCallNode::new);

    private static final Constant<EmptyNode> eofNodeParser = new Constant<>(EOF.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> endOfLineParser = new Constant<>(EOL.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> colonNodeParser = new Constant<>(COLON.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> identifierNodeParser = new Constant<>(IDENTIFIER.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> arrowNodeParser = new Constant<>(ARROW.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> leftBraceInfixNodeParser = new Constant<>(BRACES.getLeft(), new EmptyNode());

    private static final Constant<EmptyNode> rightBraceInfixNodeParser = new Constant<>(BRACES.getLeft(), new EmptyNode());

    @Override
    public List<T> parse() {
        List<T> nodes = super.parse();
        return transform(nodes);
    }

    // TODO return an AbstractSyntaxTree that has function/type definitions
    private List<T> transform(List<T> nodes) {
        List<T> transformed = new ArrayList<>();
        for (T node : nodes) {
            transformed.add(node);
        }
        return transformed;
    }

}
