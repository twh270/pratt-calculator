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
import org.byteworks.xl.parser.Pair;
import org.byteworks.xl.parser.ParseContext;
import org.byteworks.xl.parser.Parser;
import org.byteworks.xl.parser.PrefixParser;

// TODO a require(TokenType[, TokenType]) method to expect a next token type
// TODO normalize error messages
public class CalculatorParser {

    static class PrecedencePairs {
        static final Pair<Integer, Integer> EOF = new Pair<>(-1, null);
        static final Pair<Integer, Integer> EOL = new Pair<>(-1, 0);
        static final Pair<Integer, Integer> PARENS = new Pair<>(-1, 0);
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

    private enum ParserRule {
        PLUS(TokenType.PLUS, PrecedencePairs.PLUS_MINUS, new PlusPrefixParser(), new PlusInfixParser()),
        MINUS(TokenType.MINUS, PrecedencePairs.PLUS_MINUS, new MinusPrefixParser(), new MinusInfixParser()),
        MULTIPLY(TokenType.MULTIPLY, PrecedencePairs.MULT_DIV, null, new MultiplyInfixParser()),
        DIVIDE(TokenType.DIVIDE, PrecedencePairs.MULT_DIV, null, new DivideInfixParser()),
        ASSIGNMENT(TokenType.ASSIGNMENT, PrecedencePairs.ASSIGNMENT, null, new AssignmentInfixParser()),
        PLUSPLUS(TokenType.PLUSPLUS, PrecedencePairs.POST_INCREMENT, new PlusPlusPrefixParser(), new PlusPlusInfixParser()),
        MINUSMINUS(TokenType.MINUSMINUS, PrecedencePairs.POST_DECREMENT, new MinusMinusPrefixParser(), new MinusMinusInfixParser()),
        COMMA(TokenType.COMMA, PrecedencePairs.COMMA, null, new CommaInfixParser()),
        ARROW(TokenType.ARROW, PrecedencePairs.ARROW, null, null),
        COLON(TokenType.COLON, PrecedencePairs.COLON, null, null),
        EOF(TokenType.EOF, PrecedencePairs.EOF, new EofPrefixParser(), null),
        NUMBER(TokenType.NUMBER, PrecedencePairs.NUMBER, new NumberPrefixParser(), null),
        LPAREN(TokenType.LPAREN, null, new LParenPrefixParser(), null),
        IDENTIFIER(TokenType.IDENTIFIER, PrecedencePairs.IDENTIFIER, new IdentifierPrefixParser(), null),
        EOL(TokenType.EOL, PrecedencePairs.EOL, new EndOfLinePrefixParser(), null),
        FUNCTION_DEFINITION(TokenType.FUNCTION_DEFINITION, null, new FunctionDefinitionPrefixParser(), null),
        LBRACE(TokenType.LBRACE, PrecedencePairs.BRACES, new LeftBracePrefixParser(), null),
        RPAREN(TokenType.RPAREN, PrecedencePairs.PARENS, null, null),
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

    private static class EmptyNode extends Node {
    }

    static class ExpressionNode extends Node {
    }

    public static class LiteralNode extends ExpressionNode {
        private final String value;

        LiteralNode(String value) {
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

    public static class ProducesNode extends ExpressionNode {
        private final Node left;
        private final Node right;

        ProducesNode(final Node left, final Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public String toString() {
            return "(" + left + " -> " + right + ")";
        }
    }

    static class ExpressionListNode extends ExpressionNode {
        private final List<ExpressionNode> list;

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
        private final String chars;
        private final Node arguments;

        FunctionCallNode(final String chars, final Node arguments) {
            this.chars = chars;
            this.arguments = arguments;
        }

        String getChars() {
            return chars;
        }

        Node getArguments() {
            return arguments;
        }

        @Override
        public String toString() {
            return "(" + chars + " (" + arguments + "))";
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

        FunctionSignatureNode(final List<TypeExpressionNode> parameterTypes, final List<IdentifierNode> returnTypes) {
            this.parameterTypes = parameterTypes;
            this.returnTypes = returnTypes;
        }

        List<TypeExpressionNode> getParameterTypes() {
            return parameterTypes;
        }

        List<IdentifierNode> getReturnTypes() {
            return returnTypes;
        }

        @Override
        public String toString() {
            return parameterTypes.stream().map(Object::toString).collect(Collectors.joining(" ")) + " -> " +
                    returnTypes.stream().map(Object::toString).collect(Collectors.joining(" "));
        }
    }

    static class EofPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return null;
        }
    }

    static class EndOfLinePrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return parseContext.parser.parse(parseContext, PrecedencePairs.EOL.getRight());
        }
    }

    static class NumberPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return new CalculatorParser.LiteralNode(token.getChars());
        }
    }

    static class MinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            Node expr = parseContext.parser.parse(parseContext, PrecedencePairs.SIGNED.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for negative-signed");
            }
            return new NegativeSignedNode((ExpressionNode) expr);
        }
    }

    static class PlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            Node expr = parseContext.parser.parse(parseContext, PrecedencePairs.SIGNED.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for positive-signed");
            }
            return new PositiveSignedNode((ExpressionNode) expr);
        }
    }

    static class MinusMinusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            Node expr = parseContext.parser.parse(parseContext, PrecedencePairs.PRE_DECREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-decrement");
            }
            return new PreDecrementNode((ExpressionNode) expr);
        }
    }

    static class PlusPlusPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            Node expr = parseContext.parser.parse(parseContext, PrecedencePairs.PRE_INCREMENT.getRight());
            if (!(expr instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for pre-increment");
            }
            return new PreIncrementNode((ExpressionNode) expr);
        }
    }

    static class LParenPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            Node expr = parseContext.parser.parse(parseContext, PrecedencePairs.PARENS.getRight());
            Token tok = parseContext.lexer.next();
            if (!(tok.getType() == TokenType.RPAREN)) {
                throw new IllegalStateException("Expected a right parenthesis but got " + tok);
            }
            return expr;
        }
    }

    static class IdentifierPrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            return new IdentifierNode(token.getChars());
        }
    }

    static class FunctionDefinitionPrefixParser implements PrefixParser {

        private TypeExpressionNode parseTypeExpression(ParseContext parseContext) {
            Node node = parseContext.parser.parse(parseContext, PrecedencePairs.IDENTIFIER.getRight());
            if (!(node instanceof IdentifierNode)) {
                throw new IllegalStateException("Function definition type expression must be of the form identifier:type, got '" + node + "' instead");
            }
            IdentifierNode target = (IdentifierNode) node;
            if (parseContext.lexer.peek().getType() != TokenType.COLON) {
                throw new IllegalStateException("Function definition type expression must be of the form identifier:type, got'" + target + node + "' instead");
            }
            parseContext.lexer.next();
            node = parseContext.parser.parse(parseContext, PrecedencePairs.IDENTIFIER.getRight());
            if (!(node instanceof IdentifierNode)) {
                throw new IllegalStateException("Function definition type expression must be of the form identifier:type, got '" + target + ":" + node + "' instead");
            }
            IdentifierNode type = (IdentifierNode) node;
            return new TypeExpressionNode(target, type);
        }

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            List<TypeExpressionNode> parameterTypes = new ArrayList<>();
            while (parseContext.lexer.peek().getType() != TokenType.ARROW) {
                // TODO maybe param and return type is an infix parser on COLON and we expect parse(lexer, 0) to return a TypeExpressionNode
                TypeExpressionNode typeExpressionNode = parseTypeExpression(parseContext);
                parameterTypes.add(typeExpressionNode);
            }
            parseContext.lexer.next();
            List<IdentifierNode> returnTypes = new ArrayList<>();
            TokenType next = parseContext.lexer.peek().getType();
            while (next != TokenType.LBRACE && next != TokenType.ASSIGNMENT) {
                Node node = parseContext.parser.parse(parseContext, PrecedencePairs.IDENTIFIER.getRight());
                if (!(node instanceof IdentifierNode)) {
                    throw new IllegalStateException("Function definition return type(s) must be identifiers, got '" + node + "' instead");
                }
                returnTypes.add((IdentifierNode) node);
                next = parseContext.lexer.peek().getType();
            }
            Node node = parseContext.parser.parse(parseContext, 0);
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("A function implementation must be an expression, got '" + node + "' instead");
            }
            ExpressionNode body = (ExpressionNode) node;
            return new FunctionDeclarationNode(new FunctionSignatureNode(parameterTypes, returnTypes), body);
        }
    }

    static class LeftBracePrefixParser implements PrefixParser {

        @Override
        public Node parse(ParseContext parseContext, Token token) {
            List<ExpressionNode> nodes = new ArrayList<>();
            while (parseContext.lexer.peek().getType() != TokenType.RBRACE) {
                Node node = parseContext.parser.parse(parseContext, 0);
                if (!(node instanceof ExpressionNode)) {
                    throw new IllegalStateException("All elements of an expression list enclosed by { } must be an expression, but '" + node + "' is not");
                }
                nodes.add((ExpressionNode) node);
            }
            parseContext.lexer.next();
            return new ExpressionListNode(nodes);
        }
    }

    static class PlusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node rhs = parseContext.parser.parse(parseContext, PrecedencePairs.PLUS_MINUS.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to plus");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to plus");
            }
            return new PlusNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class PlusPlusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for post-increment");
            }
            return new PostIncrementNode((ExpressionNode) node);
        }
    }

    static class MinusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node rhs = parseContext.parser.parse(parseContext, PrecedencePairs.PLUS_MINUS.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to minus");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to minus");
            }
            return new MinusNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class MinusMinusInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for post-decrement");
            }
            return new PostDecrementNode((ExpressionNode) node);
        }
    }

    static class MultiplyInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node rhs = parseContext.parser.parse(parseContext, PrecedencePairs.MULT_DIV.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to multiply");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to multiply");
            }
            return new MultiplyNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class DivideInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node rhs = parseContext.parser.parse(parseContext, PrecedencePairs.MULT_DIV.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to divide");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to divide");
            }
            return new DivideNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class AssignmentInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node rhs = parseContext.parser.parse(parseContext, PrecedencePairs.ASSIGNMENT.getRight());
            if (!(node instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for lhs argument to assignment");
            }
            if (!(rhs instanceof ExpressionNode)) {
                throw new IllegalStateException("Must provide an expression for rhs argument to assignment");
            }
            return new AssignmentNode((ExpressionNode) node, (ExpressionNode) rhs);
        }
    }

    static class CommaInfixParser implements InfixParser {

        @Override
        public Node parse(ParseContext parseContext, Node node) {
            Node right = parseContext.parser.parse(parseContext, PrecedencePairs.COMMA.getRight());
            return new CommaNode(node, right);
        }
    }

    public static Parser createParser(Lexer lexer, PrintStream debugStream) {
        Parser parser = new Parser(lexer, debugStream);
        for (ParserRule rule : ParserRule.values()) {
            parser.registerParserRule(rule.tokenType, rule.precedencePair, rule.prefixParser, rule.infixParser);
        }
        return parser;
    }
}
