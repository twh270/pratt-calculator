package org.byteworks.parse.pratt;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String input;
    private int pos;

    public Lexer(String input) { this.input = input; }

    public enum TokenType {
        UNKNOWN, PLUS, PLUSPLUS, MINUS, MINUSMINUS, MULTIPLY, DIVIDE, LPAREN, RPAREN, EOF, EOL, NUMBER, IDENTIFIER,
        ASSIGNMENT;

        private static Map<String, TokenType> charsToToken = new HashMap<>();
        static {
            charsToToken.put("+", PLUS);
            charsToToken.put("++", PLUSPLUS);
            charsToToken.put("--", MINUSMINUS);
            charsToToken.put("*", MULTIPLY);
            charsToToken.put("/", DIVIDE);
            charsToToken.put("(", LPAREN);
            charsToToken.put(")", RPAREN);
            charsToToken.put("=", ASSIGNMENT);
        }
        static TokenType from(String chars) {
            TokenType type = charsToToken.get(chars);
            if (type == null) {
                throw new IllegalArgumentException("Unknown characters '" + chars +"' used to look up token type");
            }
            return type;
        }
    }

    public static class Token {
        private final String chars;
        private final TokenType type;
        public Token(String chars, TokenType type) { this.chars = chars; this.type = type; }
        public String getChars() { return chars; }
        public TokenType getType() { return type; }

        final static Eof EOF = new Eof();
        final static Eol EOL = new Eol();
        final static Operator PLUS = new Operator("+", TokenType.PLUS);
        final static Operator PLUSPLUS = new Operator("++", TokenType.PLUSPLUS);
        final static Operator MINUS = new Operator("-", TokenType.MINUS);
        final static Operator MINUSMINUS = new Operator("--", TokenType.MINUSMINUS);
        final static Operator MULT = new Operator("*", TokenType.MULTIPLY);
        final static Operator DIVIDE = new Operator("/", TokenType.DIVIDE);
        final static Operator LPAREN = new Operator("(", TokenType.LPAREN);
        final static Operator RPAREN = new Operator(")", TokenType.RPAREN);
        final static Operator ASSIGNMENT = new Operator("=", TokenType.ASSIGNMENT);

        @Override
        public String toString() {
            return "Token." + this.getClass().getSimpleName() + "(type=" + type + ", chars='" + chars + "')";
        }
    }

    public static class Number extends Token {
        Number(String chars) { super(chars, TokenType.NUMBER); }
    }

    public static class Identifier extends Token {
        Identifier(String chars) { super(chars, TokenType.IDENTIFIER); }
    }

    public static class Operator extends Token {
        Operator(String chars, TokenType tokenType) { super(chars, tokenType); }
    }

    public static class Eof extends Token {
        Eof() {
            super("", TokenType.EOF);
        }
    }

    public static class Eol extends Token {
        Eol() { super("", TokenType.EOL); }
    }

    public static class Unknown extends Token {
        Unknown(String chars) { super(chars, TokenType.UNKNOWN); }
    }

    private boolean isIdentifierCharacter(char ch) {
        return Character.isAlphabetic(ch) || '_' == ch;
    }

    public Token next() {
        if (!available()) {
            return Token.EOF;
        }
        char ch = read_ch();
        while (Character.isWhitespace(ch) && available()) {
            if (ch == '\n') {
                return Token.EOL;
            }
            ch = read_ch();
        }
        if (Character.isWhitespace(ch) && !available()) {
            return Token.EOF;
        }
        if (Character.isDigit(ch)) {
            StringBuilder sb = new StringBuilder().append(ch);
            while (available() && Character.isDigit(ch = peek_ch())) {
                sb.append(ch);
                pos++;
            }
            return new Number(sb.toString());
        } else if (isIdentifierCharacter(ch)) {
            StringBuilder sb = new StringBuilder().append(ch);
            while (available() && isIdentifierCharacter(ch = peek_ch())) {
                sb.append(ch);
                pos++;
            }
            return new Identifier(sb.toString());
        }
        else if (ch == '+') {
            if (peek_ch() == '+') {
                read_ch();
                return Token.PLUSPLUS;
            }
            return Token.PLUS;
        } else if (ch == '-') {
            if (peek_ch() == '-') {
                read_ch();
                return Token.MINUSMINUS;
            }
            return Token.MINUS;
        } else if (ch == '*') {
            return Token.MULT;
        } else if (ch == '/') {
            return Token.DIVIDE;
        } else if (ch == '(') {
            return Token.LPAREN;
        } else if (ch == ')') {
            return Token.RPAREN;
        } else if (ch == '=') {
            return Token.ASSIGNMENT;
        }
        return new Unknown(String.valueOf(ch));
    }

    public Token peek() {
        int savePos = pos;
        Token token = next();
        pos = savePos;
        return token;
    }

    private char peek_ch() {
        return input.charAt(pos);
    }

    private char read_ch() {
        return input.charAt(pos++);
    }

    private boolean available() {
        return pos < input.length();
    }
}

