import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    atic boolean hadError = false;

    blic static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } els if (ars.length == ) 

    e(args[0]);
        } els

     {
            runPrompt();

    ivate static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        
        if (hadError) System.exit(65);
    }

      InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        System.out.println("Waterflow Parser - Interactive Mode");
        System.out.println("Enter waterflow commands (Ctrl+D to exit):");

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    priva

    
    or now, just print tokens to verify scanning works
        // System.out.println("Tokens: " + tokens);

        Parser par

    hadError) return;
    
    
        // Print AST
        AstPrinter printer = new AstPrinter();
        for (Stmt statement : statements) {
            if (statement != null) {
                System.out.println(printer.print}
    

    o

    

    

      } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }

    
        
    ate static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}