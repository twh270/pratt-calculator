package org.byteworks.parse.pratt;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParserTest {

    @Test
    void parseAddition() {
        Lexer lexer = new Lexer("1 + 2");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(+ 1 2)", sb.toString());
    }

    @Test
    void parseMultiplication() {
        Lexer lexer = new Lexer("1 * 2");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(* 1 2)", sb.toString());
    }

    @Test
    void parseSimpleExpression() {
        Lexer lexer = new Lexer("1 + 2 * 3");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(+ 1 (* 2 3))", sb.toString());
    }

    @Test
    void parseComplexExpression() {
        Lexer lexer = new Lexer("4 * 1 + 2 * 3");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(+ (* 4 1) (* 2 3))", sb.toString());
    }

    @Test
    void parseNegativeNumber() {
        Lexer lexer = new Lexer("-3 + 4");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(+ -(3) 4)", sb.toString());
    }

    @Test
    void parseParenthesized() {
        Lexer lexer = new Lexer("(3 + 4) * 6");
        Parser parser = new Parser();
        List<Parser.Node> ast = parser.parse(lexer);
        StringBuilder sb = new StringBuilder();
        ast.stream().forEach(sb::append);
        Assertions.assertEquals("(* (+ 3 4) 6)", sb.toString());
    }
}
