package org.byteworks.parse.pratt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Test;

class CalculatorInterpreterTest {

    private List<Parser.Node> nodes;
    private PrintStream ps;
    private CalculatorInterpreter testObj;
    private ByteArrayOutputStream baos;

    private void setUp(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = CalculatorParser.createParser();
        nodes = parser.parse(lexer);
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        testObj = new CalculatorInterpreter();
    }

    private String execute(String input) {
        setUp(input);
        testObj.exec(nodes, ps);
        ps.flush();
        return new String(baos.toByteArray());
    }

    @Test
    void interpretsAddition() {
        String result = execute("1 + 2");
        assertEquals("3: Number\n", result);
    }

    @Test
    void interpretsSimpleExpression() {
        String result = execute("1 + 2 * 3");
        assertEquals("7: Number\n", result);
    }

    @Test
    void interpretsComplexExpression() {
        String result = execute("4 * 1 + 2 * 3");
        assertEquals("10: Number\n", result);
    }

    @Test
    void interpretsComplexExpressionAllOperators() {
        String result = execute("3 * 4 + 6 - 8 / 2");
        assertEquals("14: Number\n", result);
    }

    @Test
    void interpretsExpressionWithNegativeSignedNumber() {
        String result = execute("-3 + 4");
        assertEquals("1: Number\n", result);
    }

    @Test
    void interpretsExpressionWithPositiveSignedNumber() {
        String result = execute("3 + +4");
        assertEquals("7: Number\n", result);
    }

    @Test
    void interpretsParenthesized() {
        String result = execute("(3 + 4) * 2");
        assertEquals("14: Number\n", result);
    }

    @Test
    void interpretsVariableAssignment() {
        String result = execute("x = 3 + 4");
        assertEquals("7: Number\n", result);
        assertEquals("7: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsVariableAssignmentAndEvaluation() {
        String result = execute("x = 3 + 4\nx * 2");
        assertEquals("7: Number\n14: Number\n", result);
        assertEquals("7: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPreIncrementNumber() {
        String result = execute("++4");
        assertEquals("5: Number\n", result);
    }

    @Test
    void interpretsPreDecrementNumber() {
        String result = execute("--4");
        assertEquals("3: Number\n", result);
    }

    @Test
    void interpretsPreIncrementVariable() {
        String result = execute("x = 3 + 4\n++x");
        assertEquals("7: Number\n8: Number\n", result);
        assertEquals("8: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPreDecrementVariable() {
        String result = execute("x = 3 + 4\n--x");
        assertEquals("7: Number\n6: Number\n", result);
        assertEquals("6: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPostIncrementNumber() {
        String result = execute("4++");
        assertEquals("4: Number\n", result);
    }

    @Test
    void interpretsPostIncrementVariable() {
        String result = execute("x = 3 + 4\nx++");
        assertEquals("7: Number\n7: Number\n", result);
        assertEquals("8: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPostDecrementNumber() {
        String result = execute("4--");
        assertEquals("4: Number\n", result);
    }

    @Test
    void interpretsPostDecrementVariable() {
        String result = execute("x = 3 + 4\nx--");
        assertEquals("7: Number\n7: Number\n", result);
        assertEquals("6: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPostIncrementPrecedence() {
        String result = execute("x = 3\n2+++x");
        assertEquals("3: Number\n5: Number\n", result);
        assertEquals("3: Number", testObj.getVariable("x").toString());
    }

    @Test
    void interpretsPostDecrementPrecedence() {
        String result = execute("x = 3\nx---2");
        assertEquals("3: Number\n1: Number\n", result);
        assertEquals("2: Number", testObj.getVariable("x").toString());
    }

}
