package org.byteworks.xl.interpreter;

import org.byteworks.parser.Node;
import org.byteworks.parser.Parser;
import org.byteworks.lexer.Lexer;
import org.byteworks.xl.parser.XLParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XLInterpreterTest {

    private List<Node> nodes;
    private PrintStream ps;
    private XLInterpreter testObj;
    private ByteArrayOutputStream baos;

    private void setUp(String input) {
        Lexer lexer = new Lexer(input);
        Parser<Node> parser = XLParser.createParser(lexer, System.out);
        nodes = parser.parse();
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        testObj = new XLInterpreter();
    }

    private String execute(String input) {
        setUp(input);
        testObj.exec(nodes, ps);
        ps.flush();
        return new String(baos.toByteArray());
    }

    @ParameterizedTest(name = "{index} {0}")
    @CsvSource({
            "addition, '1 + 2', '3: Number\n'",
            "expression 1, '1 + 2 * 3', '7: Number\n'",
            "expression 2, '4 * 1 + 2 * 3', '10: Number\n'",
            "expression - all operators, '3 * 4 + 6 - 8 / 2', '14: Number\n'",
            "expression - negative number, '-3 + 4', '1: Number\n'",
            "expression - positive number, '3 + +4', '7: Number\n'",
            "parenthesized, '(3 + 4) * 2', '14: Number\n'",
            "pre-increment, '++4', '5: Number\n'",
            "pre-decrement, '--4', '3: Number\n'",
            "post-increment, '4++', '4: Number\n'",
            "post-decrement, '4--', '4: Number\n'"
    })
    void interpretsSimpleInput(String name, String input, String expectedOutput) {
        String result = execute(input);
        assertEquals(expectedOutput, result);
    }

    @ParameterizedTest(name = "{index} {0}")
    @CsvSource({
            "simple assignment, 'x = 3 + 4', '7: Number\n', '7: Number'",
            "assign and evaluate, 'x = 3 + 4\nx * 2', '7: Number\n14: Number\n', '7: Number'",
            "pre-increment, 'x = 3 + 4\n++x', '7: Number\n8: Number\n', '8: Number'",
            "pre-decrement, 'x = 3 + 4\n--x', '7: Number\n6: Number\n', '6: Number'",
            "post-increment, 'x = 3 + 4\nx++', '7: Number\n7: Number\n', '8: Number'",
            "post-decrement, 'x = 3 + 4\nx--', '7: Number\n7: Number\n', '6: Number'",
            "post-increment precedence, 'x = 3\n2+++x', '3: Number\n5: Number\n', '3: Number'",
            "post-decrement precedence, 'x = 3\nx---2', '3: Number\n1: Number\n', '2: Number'"
    })
    void interpretsVariableExpressions(String name, String input, String expectedOutput, String expectedVariableValue) {
        String result = execute(input);
        assertEquals(expectedOutput, result);
        assertEquals(expectedVariableValue, testObj.interpreter.getVariable("x").toString());
    }

    @Test
    void interpretsFunctionDefinition() {
        String result = execute("f = fn x:Number y:Number -> Number { x + y }");
        Type number = testObj.interpreter.getType("Number");
        Function fn = testObj.interpreter.getFunction("f", new TypeList(List.of(number, number)));
        assertEquals("(Number, Number -> Number)", fn.getSignature().toString());
        assertEquals("(Number, Number -> Number): Number, Number\n", result);
    }

    @ParameterizedTest(name = "{index} {0}")
    @CsvSource({
            "function call 1, 'f = fn x:Number y:Number -> Number { x + y }\nf(3, 4)', '(Number, Number -> Number): Number, Number\n7: Number\n'",
            "function call 2, 'f = fn -> Number { 3 }\nf()', '(Unit -> Number): Unit\n3: Number\n'",
            "function call 3, 'f = fn x:Number -> Number { x * 10 }\nf(6)', '(Number -> Number): Number\n60: Number\n'",
            "function call 4, 'f = fn x:Number y:Number z:Number -> Number {x + y + z}\nf(6, 3 * 4, 2)', '(Number, Number, Number -> Number): Number, Number, Number\n20: Number\n'",
            "function call 5, 'f = fn -> { }', '(Unit -> Unit): Unit\n'",
            "function call 5, 'f = fn x:Number -> Number { x + 10 }\nn = { 3 * 4\n4 + 2 }\nf(n)', '(Number -> Number): Number\n6: Number\n16: Number\n'"
    })
    void executesFunctionCall(String name, String code, String expected) {
        String result = execute(code);
        assertEquals(expected, result);
    }
}
