package org.byteworks.parse.pratt;

import java.util.HashMap;
import java.util.Map;

public class Lexer {
    private final String input;
    private int pos;

    public Lexer(String input) { this.input = input; }

    public enum TokenType {
        UNKNOWN, PLUS, MINUS, MULTIPLY, DIVIDE, LPAREN, RPAREN, EOF, EOL, NUMBER, IDENTIFIER,
        ASSIGNMENT;

        private static Map<String, TokenType> charsToToken = new HashMap<>();
        static {
            charsToToken.put("+", PLUS);
            charsToToken.put("-", MINUS);
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
        public Token(String chars) { this.chars = chars; this.type = TokenType.from(chars); }
        public Token(String chars, TokenType type) { this.chars = chars; this.type = type; }
        public String getChars() { return chars; }
        public TokenType getType() { return type; }

        final static Eof EOF = new Eof();
        final static Eol EOL = new Eol();
        final static Plus PLUS = new Plus();
        final static Minus MINUS = new Minus();
        final static Mult MULT = new Mult();
        final static Divide DIVIDE = new Divide();
        final static LParen LPAREN = new LParen();
        final static RParen RPAREN = new RParen();
        final static Assignment ASSIGNMENT = new Assignment();

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

    public static class Plus extends Token {
        Plus() { super("+"); }
    }

    public static class Minus extends Token {
        public Minus() { super("-"); }
    }

    public static class Mult extends Token {
        Mult() { super("*"); }
    }

    public static class Divide extends Token {
        Divide() { super("/"); }
    }

    public static class LParen extends Token {
        LParen() { super("("); }
    }

    static class RParen extends Token {
        RParen() { super(")"); }
    }

    static class Assignment extends Token {
        Assignment() { super("="); }
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
            return Token.PLUS;
        } else if (ch == '-') {
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

