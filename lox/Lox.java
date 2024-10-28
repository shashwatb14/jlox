// https://www.craftinginterpreters.com/scanning.html

package lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    static boolean hadError = false;

    // main method
    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); // from UNIX "sysexist.h"
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    // run file if provided
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // indicate error in exit code
        if (hadError) System.exit(65);
    }

    // repl
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        while (true) {
            System.out.println("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);

            // avoid ending session
            hadError = false;
        }
    }

    // run
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // temporarily just prints tokens
        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    // error handling
    static void error(int line, int column, String message, String location) {
        report(line, column, "", message, location);
    }

    private static void report(int line, int column, String where, 
                                String message, String location) {
        System.err.printf("[line: %d] Error %s: %s\n\n", line, where, message);
        System.err.println(location);
        System.err.println(String.valueOf("-").repeat(column - 1) + "^");
        hadError = true;
    }
}