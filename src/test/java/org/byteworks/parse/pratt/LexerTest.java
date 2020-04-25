package org.byteworks.parse.pratt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LexerTest {
    @Test
    void parsesPaddedNumber() {
        Lexer testObj = new Lexer("  23  ");
        Lexer.Token next = testObj.next();
        Assertions.assertTrue(next instanceof Lexer.Number);
        Lexer.Number num = (Lexer.Number) next;
        Assertions.assertEquals("23", num.getChars());
    }
}
