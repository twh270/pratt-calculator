package org.byteworks.parse.pratt;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InterpreterTest {

    private List<Parser.Node> nodes;
    private PrintStream ps;
    private Interpreter testObj;
    private ByteArrayOutputStream baos;

    private void setUp(String input) {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser();
        nodes = parser.parse(lexer);
        baos = new ByteArrayOutputStream();
        ps = new PrintStream(baos);
        testObj = new Interpreter();
    }

    @Test
    void interpretsAddition() {
        setUp("1 + 2");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("3: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsSimpleExpression() {
        setUp("1 + 2 * 3");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("7: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsComplexExpression() {
        setUp("4 * 1 + 2 * 3");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("10: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsComplexExpressionAllOperators() {
        setUp("3 * 4 + 6 - 8 / 2");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("14: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsExpressionWithNegativeSignedNumber() {
        setUp("-3 + 4");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("1: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsExpressionWithPositiveSignedNumber() {
        setUp("3 + +4");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("7: NUMBER\n", new String(baos.toByteArray()));
    }

    @Test
    void interpretsParenthesized() {
        setUp("(3 + 4) * 2");
        testObj.exec(nodes, ps);
        ps.flush();
        Assertions.assertEquals("14: NUMBER\n", new String(baos.toByteArray()));
    }
}
