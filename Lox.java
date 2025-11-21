import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Lox {
    static boolean hadError = false;
    static boolean hadRuntimeError = false;
    private static double rainfallMm = 1.0;
    private static int days = 1;

    public static void main(String[] args) throws IOException {
        if (args.length > 3) {
            System.out.println("Usage: jlox [script] [days] [rainfallMm]");
            System.exit(64);
        } else if (args.length >= 1) {
            if (args.length == 2) {
                try {
                    rainfallMm = Double.parseDouble(args[1]);
                } catch (NumberFormatException ex) {
                    System.out.println("Rainfall must be a number.");
                    System.exit(64);
                }
            } else if (args.length == 3) {
                try {
                    days = Integer.parseInt(args[1]);
                    rainfallMm = Double.parseDouble(args[2]);
                    if (days < 1) {
                        throw new NumberFormatException("Days must be positive.");
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Usage: jlox [script] [days] [rainfallMm]");
                    System.exit(64);
                }
            }
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, StandardCharsets.UTF_8));

        if (hadError) System.exit(65);
        if (hadRuntimeError) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
            hadRuntimeError = false;
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError) return;

        Map<String, Interpreter.RiverState> sharedRivers = new LinkedHashMap<>();
        for (int day = 1; day <= days; day++) {
            Interpreter interpreter = new Interpreter(rainfallMm, day, days, sharedRivers);
            interpreter.interpret(statements);
            if (hadRuntimeError) break;
        }
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line " + error.token.line + "]");
        hadRuntimeError = true;
    }
}
