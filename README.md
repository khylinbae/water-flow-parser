# Water Flow Parser

This repository contains a Java implementation of a small language front-end. The `Lox` entry point reads source code, feeds it through the scanner, parser, and AST printer, and writes the resulting abstract syntax tree to standard output.

## Running an example

Compile the sources:

```sh
javac *.java
```

Then execute an example file, such as `Examples/example1.wflow`:

```sh
java Lox Examples/example1.wflow
```

The runtime will:

1. Use `Scanner` to convert the raw characters into `Token` objects.
2. Send the tokens to `Parser` to produce a list of statements (`Stmt`).
3. Walk the resulting statements with `AstPrinter` to print the tree representation.

If any scanning or parsing errors occur, they are reported to standard error and execution stops before printing the AST.
