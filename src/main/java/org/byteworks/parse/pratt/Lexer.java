package org.byteworks.parse.pratt;

public class Lexer {
    private final String input;
    private int pos;

    public Lexer(String input) { this.input = input; }

    public static class Token {
        private final String chars;
        public Token() { this.chars = ""; }
        public Token(String chars) { this.chars = chars; }
        public String getChars() { return chars; }

        final static Eof EOF = new Eof();
        final static Plus PLUS = new Plus();
        final static Minus MINUS = new Minus();
        final static Mult MULT = new Mult();
        final static Divide DIVIDE = new Divide();
        final static LParen LPAREN = new LParen();
        final static RParen RPAREN = new RParen();

        @Override
        public String toString() {
            return "Token." + this.getClass().getSimpleName() + "(" + chars + ")";
        }
    }

    public static class Number extends Token {
        public Number(String chars) { super(chars); }
    }

    public static class Eof extends Token {
    }

    public static class Unknown extends Token {
        public Unknown(String chars) { super(chars); }
    }

    public static class Plus extends Token {
        public Plus() { super("+"); }
    }

    public static class Minus extends Token {
        public Minus() { super("-"); }
    }

    public static class Mult extends Token {
        public Mult() { super("*"); }
    }

    public static class Divide extends Token {
        public Divide() { super("/"); }
    }

    public static class LParen extends Token {
        public LParen() { super("("); }
    }

    public static class RParen extends Token {
        public RParen() { super(")"); }
    }

    public Token next() {
        if (!available()) {
            return Token.EOF;
        }
        char ch = read_ch();
        while (Character.isWhitespace(ch) && available()) {
            ch = read_ch();
        }
        if (Character.isWhitespace(ch) && !available()) {
            return Token.EOF;
        }
        if (Character.isDigit(ch)) {
            StringBuilder sb = new StringBuilder().append(ch);
            while (available() && Character.isDigit(ch = read_ch())) {
                sb.append(ch);
            }
            if (available()) { pos--; }
            return new Number(sb.toString());
        } else if (ch == '+') {
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
        }
        return new Unknown(String.valueOf(ch));
    }

    public Token peek() {
        int savePos = pos;
        Token token = next();
        pos = savePos;
        return token;
    }

    private char read_ch() {
        return input.charAt(pos++);
    }

    private boolean available() {
        return pos < input.length();
    }
}

