package org.byteworks.xl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Parser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class CalculatorParserTest {

    @ParameterizedTest(name = "{index} {0}")
    @CsvSource({
            "addition, '1 + 2', '(+ 1 2)'",
            "multiplication, '1 * 2', '(* 1 2)'",
            "simple expression 1, '1 + 2 * 3', '(+ 1 (* 2 3))'",
            "simple expression 2, '4 * 1 + 2 * 3', '(+ (* 4 1) (* 2 3))'",
            "negative number, '-3 + 4', '(+ -(3) 4)'",
            "positive number, '3 + +4', '(+ 3 +(4))'",
            "parenthesized 1, '(3 + 4)', '(+ 3 4)'",
            "parenthesized 2, '(3 + 4) * 6', '(* (+ 3 4) 6)'",
            "variable assignment 1, 'x = 3', '(= x 3)'",
            "variable assignment 2, 'x = 3 * (4 + 9)', '(= x (* 3 (+ 4 9)))'",
            "end of line, 'x = 3\nx * 2', '(= x 3)(* x 2)'",
            "pre-increment, '++4', '++(4)'",
            "pre-decrement, '--4', '--(4)'",
            "post-increment, '4++', '++(4)'",
            "post-decrement, '4--', '--(4)'",
            "function definition, 'f = fn x:Number y:Number -> Number { x + y }', '(= f fn x:Number y:Number -> Number { (+ x y) })'",
            "function call, 'f 3 4', '(f (3, 4))'"
    })
    void parsesInput(String name, String input, String expected) {
        Lexer lexer = new Lexer(input);
        Parser parser = CalculatorParser.createParser(lexer, System.out);
        List<Node> ast = parser.parse();
        StringBuilder sb = new StringBuilder();
        ast.forEach(sb::append);
        assertEquals(expected, sb.toString());
    }

}
