package org.byteworks.xl.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class LexerTest {
    @Test
    void parsesPaddedNumber() {
        Lexer testObj = new Lexer("  23  ");
        Token next = testObj.next();
        Assertions.assertTrue(next instanceof Lexer.Number);
        Lexer.Number num = (Lexer.Number) next;
        Assertions.assertEquals("23", num.getChars());
    }
}
