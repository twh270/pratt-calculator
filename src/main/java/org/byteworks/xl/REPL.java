package org.byteworks.xl;

import org.byteworks.parser.Node;
import org.byteworks.parser.Parser;
import org.byteworks.xl.interpreter.XLInterpreter;
import org.byteworks.lexer.Lexer;

import java.util.List;
import java.util.Scanner;

public class REPL {
    public static void main(String[] args) {
        XLInterpreter interpreter = new XLInterpreter();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            if("quit".equalsIgnoreCase(input)) {
                return;
            }
            Parser<Node> parser = new Parser<>(new Lexer(input), System.out);
            List<Node> nodes = parser.parse();
            interpreter.exec(nodes, System.out);
        }
    }
}
