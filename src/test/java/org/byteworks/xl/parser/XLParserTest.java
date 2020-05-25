package org.byteworks.xl.parser;

import org.byteworks.lexer.Lexer;
import org.byteworks.parser.Node;
import org.byteworks.parser.Parser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.stream.Collectors;

class XLParserTest {

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
            "end of line, 'x = 3\nx * 2', '(= x 3), (* x 2)'",
            "pre-increment, '++4', '++(4)'",
            "pre-decrement, '--4', '--(4)'",
            "post-increment, '4++', '++(4)'",
            "post-decrement, '4--', '--(4)'",
            "function definition 1, 'f = fn x:Number y:Number -> Number { x + y }', '(= f fn x:Number y:Number -> Number { (+ x y) })'",
            "function definition 2, 'f = fn -> Number { 42 }', '(= f fn -> Number { 42 })'",
            "function definition 3, 'f = fn -> { g() }', '(= f fn -> { (g ()) })'",
            "function call 1, 'f(3, 4)', '(f (3, 4))'",
            "function call 2, 'f()', '(f ())'",
            "function call 3, 'f(2)', '(f (2))'",
            "function call 4, 'f(3, 3 * 4, 5)', '(f (3, (* 3 4), 5))'",
            "expr list 1, 'x = { 3 * 4\n4 + 2 }', '(= x { (* 3 4),(+ 4 2) })'",
            "function call with expr list, 'f = fn x:Number -> Number { x + 10 }\nn = { 3 * 4\n4 + 2 }\nf(n)', '(= f fn x:Number -> Number { (+ x 10) }), (= n { (* 3 4),(+ 4 2) }), (f (n))'",
    })
    void parsesInput(String name, String input, String expected) {
        Lexer lexer = new Lexer(input);
        Parser<Node> parser = XLParser.createParser(lexer, System.out);
        List<Node> ast = parser.parse();
        String result = ast.stream().map(Object::toString).collect(Collectors.joining(", "));
        Assertions.assertEquals(expected, result);
    }

}
