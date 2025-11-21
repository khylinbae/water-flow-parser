## Documentation

- (./waterflow-parser/doc.pdf)

## Running the interpreter

Compile the project and run any `.wflow` program:

```bash
javac *.java
java Lox Examples/example1.wflow                # defaults to 1 day, 1 mm
java Lox Examples/example2.wflow 3 2.5          # simulate 3 days with 2.5 mm rain each
java Lox Examples/example3.wflow 5              # 5 days at the default 1 mm rain
```

If you do not pass a rainfall argument the interpreter injects a global `rainfall`
variable with the default value `1.0`. Programs can refer to `rainfall` inside
expressions when declaring rivers or dams. The optional second numeric argument
lets you set the **day count** so the interpreter repeats the program for multiple
days and prints the flows for each day.

### Step-by-step setup and execution

1. **Install Java** (JDK 8+). Any recent OpenJDK should work since the project is
   plain Java.
2. **Navigate to the repo root**:
   ```bash
   cd /workspace/water-flow-parser
   ```
3. **Compile** all sources (produces `.class` files in the same directory):
   ```bash
   javac *.java
   ```
4. **Run a sample program**. You can omit extra arguments to use one day and the
   default `1.0` mm rainfall, or supply the day count followed by rainfall (in
   mm):
   ```bash
   java Lox Examples/example1.wflow             # 1 day, 1.0 mm rainfall
   java Lox Examples/example2.wflow 3 2.5       # 3 days, 2.5 mm rainfall per day
   ```
5. **Inspect the output**:
   - Any `output` or `print` statements display immediately while the program
     executes.
   - A final summary lists each river’s flow and the applied dam factors to help
     you verify the program behavior.

## Language overview

The language now executes the AST instead of only printing it. Each program
produces:

1. Outputs from any `print` or `output` statements encountered while the program
   runs.
2. A per-day summary that lists every river (including implicit ones created via
   `flow` statements) together with the amount of flow remaining after all dams
   and flows have been processed. Dam levels are carried forward across days so
   you can model storage and release.

### Rivers and flows

- `river <name> = <expr>;` creates a river with the given base flow. If the
  initializer is omitted the base flow defaults to the rainfall amount.
- `combine <name> = r1 + r2 + ...;` creates a river whose flow is the sum of
  other rivers.
- `<source> -> <target>;` directs the full flow from `source` into `target`.
- `output <name>;` prints the current flow for a river immediately.

### Dams

Dams control river flow by scaling the amount of water that leaves the river.
The syntax is intentionally small and maps directly onto the rubric
requirements:

```
dam <river> open;        // allow 100% of the flow
dam <river> close;       // hold all of the water back
dam <river> adjust expr; // scale by the value of expr (0.5 halves, 1.2 boosts)

While evaluating the adjustment expression, the interpreter injects helper
variables so a dam algorithm can depend on multiple inputs:

- `inflow` — the river's current incoming flow before the dam.
- `rainfall` — today's rainfall (the CLI-supplied value or the default 1.0 mm).
- `damLevel` — persistent reservoir storage for this dam, which is increased by
  rain and inflow that the dam holds back.
The globals `rainfall` and `day` are defined at the start of every simulation day
so algorithms can react to the weather and the current step of the run.

### Full grammar

```
program        -> declaration* EOF ;
declaration    -> riverDecl
                | combineDecl
                | flowDecl
                | damDecl
                | varDecl
                | statement ;
riverDecl      -> "river" IDENTIFIER ( "=" expression )? ";" ;
combineDecl    -> "combine" IDENTIFIER "=" IDENTIFIER ( "+" IDENTIFIER )* ";" ;
flowDecl       -> IDENTIFIER "->" IDENTIFIER ";" ;
damDecl        -> "dam" IDENTIFIER damMode ";" ;
damMode        -> "open" | "close" | "adjust" expression ;
varDecl        -> "var" IDENTIFIER ( "=" expression )? ";" ;
statement      -> outputStmt
                | printStmt
                | block
                | expressionStmt ;
outputStmt     -> "output" IDENTIFIER ";" ;
printStmt      -> "print" expression ";" ;
block          -> "{" declaration* "}" ;
expressionStmt -> expression ";" ;
expression     -> equality ;
equality       -> comparison ( ( "!=" | "==" ) comparison )* ;
comparison     -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
term           -> factor ( ( "-" | "+" ) factor )* ;
factor         -> unary ( ( "/" | "*" ) unary )* ;
unary          -> ( "!" | "-" ) unary | primary ;
primary        -> NUMBER | STRING | IDENTIFIER | "true" | "false" | "nil"
                | "(" expression ")" ;
```
```

`adjust` accepts any arithmetic expression. Supplying a number bigger than one
simulates increasing the flow; supplying a number between zero and one reduces
the flow, and zero stops the flow entirely. Negative factors are rejected with a
runtime error so that programs cannot create backwards rivers.

## Example output

Running `java Lox Examples/example1.wflow 2` prints two daily snapshots:

```
lower_molongolo flow: 0.00 L/s

=== Day 1 of 2 with 1.0 mm rainfall ===
...
== River flows after day 1 ==
googong              10.00 L/s (dam 1.00x, level 0.00 m3)
dam1                 5.00 L/s (dam 0.50x, level 3.00 m3)
...
=== Day 2 of 2 with 1.0 mm rainfall ===
== River flows after day 2 ==
googong              10.00 L/s (dam 1.00x, level 0.00 m3)
dam1                 5.00 L/s (dam 0.50x, level 6.00 m3)
...
```

The summary makes grading/debugging straightforward because every example program
clearly communicates how much water ends up in each river.

<details>
<summary><b>Waterflow Parser Specification</b></summary>

| Question | Answer |
|-----------|---------|
| **What literal in your language represents a river that gets 10 L/s of flow on the first day after 1 mm of rainfall?** | `10` |
| **What symbol in your language is used to show two rivers combine?** | `'+'` or `'->'`<br>• The `+` operator combines them.<br>• The `->` operator directs flow from one river to another. |
| **Is the above symbol a "unary", "binary", or "literal"?** | Binary |
| **What folder is the "working folder" to compile your parser?** | `Waterflow-parser` |
| **What command(s) will compile your parser?** | ```bash<br>javac *.java<br>java Lox examples/example1.wflow<br>```<br>(It can also be `example2` or `example3`.) |
| **In your language, how long does it take all the water to work through a river system after 1 day of rain?** | 10 days |
| **Does your language include statements or is it an expression language?** | It is a **statement-based** language.<br>The parser walks the token stream into a list of `Stmt` nodes and recognizes constructs such as `river`, `output`, `combine`, `flow` declarations, `print`, `blocks`, and expression statements—all of which are statement forms.<br>The AST explicitly defines corresponding `Stmt` subclasses, confirming that top-level and nested constructs are represented as statements rather than just expressions. |
| **Which chapter of the book have you used as the starting point for your solution?** | Mainly **Chapter 5** and **Chapter 6**, but **Chapter 8** as well. |

</details>
