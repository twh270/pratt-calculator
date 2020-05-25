package org.byteworks.lexer;

public class Token {
    private final String chars;
    private final TokenType type;
    Token(String chars, TokenType type) { this.chars = chars; this.type = type; }
    public String getChars() { return chars; }
    public TokenType getType() { return type; }

    @Override
    public String toString() {
        return "Token." + this.getClass().getSimpleName() + "(type=" + type + ", chars='" + chars + "')";
    }
}
