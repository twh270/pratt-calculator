package org.byteworks.lexer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Lexer {
    private final String input;
    private int pos;

    public Lexer(String input) { this.input = input; }

    private static class Tokens {
        final static Eof EOF = new Eof();
        final static Eol EOL = new Eol();
        final static Operator PLUS = new Operator("+", TokenType.PLUS);
        final static Operator PLUSPLUS = new Operator("++", TokenType.PLUSPLUS);
        final static Operator MINUS = new Operator("-", TokenType.MINUS);
        final static Operator MINUSMINUS = new Operator("--", TokenType.MINUSMINUS);
        final static Operator MULTIPLY = new Operator("*", TokenType.MULTIPLY);
        final static Operator DIVIDE = new Operator("/", TokenType.DIVIDE);
        final static Operator LPAREN = new Operator("(", TokenType.LPAREN);
        final static Operator RPAREN = new Operator(")", TokenType.RPAREN);
        final static Operator ASSIGNMENT = new Operator("=", TokenType.ASSIGNMENT);
        final static Operator COMMA = new Operator(",", TokenType.COMMA);
        final static Operator ARROW = new Operator("->", TokenType.ARROW);
        final static Operator LBRACE = new Operator("{", TokenType.LBRACE);
        final static Operator RBRACE = new Operator("}", TokenType.RBRACE);
        final static Operator COLON = new Operator(":", TokenType.COLON);
        final static Keyword FUNCTION_DEFINITION = new Keyword("fn", TokenType.FUNCTION_DEFINITION);
    }

    static class Number extends Token {
        Number(String chars) { super(chars, TokenType.NUMBER); }
    }

    static class Identifier extends Token {
        Identifier(String chars) { super(chars, TokenType.IDENTIFIER); }
    }

    static class Keyword extends Token {
        Keyword(String chars, TokenType tokenType) { super(chars, tokenType); }
    }

    static class Operator extends Token {
        Operator(String chars, TokenType tokenType) { super(chars, tokenType); }
    }

    static class Eof extends Token {
        Eof() {
            super("", TokenType.EOF);
        }
    }

    static class Eol extends Token {
        Eol() { super("", TokenType.EOL); }
    }

    static class Unknown extends Token {
        Unknown(String chars) { super(chars, TokenType.UNKNOWN); }
    }

    private boolean isIdentifierOrSymbolCharacter(char ch) {
        return Character.isAlphabetic(ch) || '_' == ch;
    }

    public boolean hasMoreTokens() {
        return !(peek() == Tokens.EOF);
    }

    public Token next() {
        if (!available()) {
            return Tokens.EOF;
        }
        char ch = read_ch();
        while (Character.isWhitespace(ch) && available()) {
            if (ch == '\n') {
                return Tokens.EOL;
            }
            ch = read_ch();
        }
        if (Character.isWhitespace(ch) && !available()) {
            return Tokens.EOF;
        }
        if (Character.isDigit(ch)) {
            StringBuilder sb = new StringBuilder().append(ch);
            while (available() && Character.isDigit(ch = peek_ch())) {
                sb.append(ch);
                pos++;
            }
            return new Number(sb.toString());
        } else if (isIdentifierOrSymbolCharacter(ch)) {
            StringBuilder sb = new StringBuilder().append(ch);
            while (available() && isIdentifierOrSymbolCharacter(ch = peek_ch())) {
                sb.append(ch);
                pos++;
            }
            String tokenString = sb.toString();
            if (Tokens.FUNCTION_DEFINITION.getChars().equals(tokenString)) {
                return Tokens.FUNCTION_DEFINITION;
            }
            return new Identifier(tokenString);
        }
        else if (ch == '+') {
            if (peek_ch() == '+') {
                read_ch();
                return Tokens.PLUSPLUS;
            }
            return Tokens.PLUS;
        } else if (ch == '-') {
            if (consumeIf('-')) {
                return Tokens.MINUSMINUS;
            } else if (consumeIf('>')) {
                return Tokens.ARROW;
            }
            return Tokens.MINUS;
        } else if (ch == '*') {
            return Tokens.MULTIPLY;
        } else if (ch == '/') {
            return Tokens.DIVIDE;
        } else if (ch == '(') {
            return Tokens.LPAREN;
        } else if (ch == ')') {
            return Tokens.RPAREN;
        } else if (ch == '=') {
            return Tokens.ASSIGNMENT;
        } else if (ch == ',') {
            return Tokens.COMMA;
        } else if (ch == '{') {
            return Tokens.LBRACE;
        } else if (ch == '}') {
            return Tokens.RBRACE;
        } else if (ch == ':') {
            return Tokens.COLON;
        }
        return new Unknown(String.valueOf(ch));
    }

    public Token peek() {
        int savePos = pos;
        Token token = next();
        pos = savePos;
        return token;
    }

    public boolean peekIs(TokenType... tokenTypes) {
        Token token = peek();
        for (TokenType tokenType : tokenTypes) {
            if (token.getType() == tokenType) {
                return true;
            }
        }
        return false;
    }

    public boolean consumeIf(TokenType... tokenTypes) {
        if (peekIs(tokenTypes)) {
            next();
            return true;
        }
        return false;
    }

    public List<Token> consumeUntil(final TokenType... tokenTypes) {
        List<Token> tokens = new ArrayList<>();
        while(true) {
            Token token = peek();
            if (peekIs(tokenTypes)) {
                return tokens;
            }
            if (token.getType() == TokenType.EOF) {
                throw new IllegalStateException("Reached EOF while scanning for token type(s) " + Arrays.toString(tokenTypes));
            }
            tokens.add(next());
        }
    }

    private char peek_ch() {
        return input.charAt(pos);
    }

    private boolean consumeIf(char tokenChar) {
        char ch = peek_ch();
        if (tokenChar == ch) {
            read_ch();
            return true;
        }
        return false;
    }

    private char read_ch() {
        return input.charAt(pos++);
    }

    private boolean available() {
        return pos < input.length();
    }
}

