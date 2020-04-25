package org.byteworks.parse.pratt;

import java.util.List;
import java.util.Scanner;

public class REPL {
    public static void main(String[] args) {
        Parser parser = new Parser();
        Interpreter interpreter = new Interpreter();

        Scanner scanner = new Scanner(System.in);
        while(true) {
            String input = scanner.nextLine();
            List<Parser.Node> nodes = parser.parse(new Lexer(input));
            interpreter.exec(nodes, System.out);
        }
    }
}
