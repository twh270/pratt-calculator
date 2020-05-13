package org.byteworks.xl;

import java.util.List;
import java.util.Scanner;

import org.byteworks.xl.lexer.Lexer;
import org.byteworks.xl.parser.Node;
import org.byteworks.xl.parser.Parser;

public class REPL {
    public static void main(String[] args) {
        XLInterpreter interpreter = new XLInterpreter();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            if("quit".equalsIgnoreCase(input)) {
                return;
            }
            Parser parser = new Parser(new Lexer(input), System.out);
            List<Node> nodes = parser.parse();
            interpreter.exec(nodes, System.out);
        }
    }
}
