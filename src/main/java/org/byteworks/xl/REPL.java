package org.byteworks.xl;

import java.util.List;
import java.util.Scanner;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Parser;

public class REPL {
    public static void main(String[] args) {
        Parser parser = CalculatorParser.createParser();
        CalculatorInterpreter interpreter = new CalculatorInterpreter();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            if("quit".equalsIgnoreCase(input)) {
                return;
            }
            List<Node> nodes = parser.parse(new Lexer(input));
            interpreter.exec(nodes, System.out);
        }
    }
}
