## Documentation

- (./waterflow-parser/doc.pdf)

## Running the interpreter

Compile the project and run any `.wflow` program:

```bash
javac *.java
java Lox Examples/example1.wflow           # defaults to 1 mm of rainfall
java Lox Examples/example2.wflow 2.5       # override rainfall (in mm)
```

If you do not pass a rainfall argument the interpreter injects a global `rainfall`
variable with the default value `1.0`. Programs can refer to `rainfall` inside
expressions when declaring rivers or dams.

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
4. **Run a sample program**. You can omit the rainfall argument to use the
   default `1.0` mm or supply a value (in mm) as the second argument:
   ```bash
   java Lox Examples/example1.wflow       # uses 1.0 mm rainfall
   java Lox Examples/example2.wflow 2.5   # overrides rainfall to 2.5 mm
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
2. A final summary that lists every river (including implicit ones created via
   `flow` statements) together with the amount of flow remaining after all dams
   and flows have been processed.

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
```

`adjust` accepts any arithmetic expression. Supplying a number bigger than one
simulates increasing the flow; supplying a number between zero and one reduces
the flow, and zero stops the flow entirely. Negative factors are rejected with a
runtime error so that programs cannot create backwards rivers.

## Example output

Running `java Lox Examples/example1.wflow` prints:

```
lower_molongolo flow: 0.00 L/s

== Final river flows with 1.0 mm rainfall ==
googong              10.00 L/s (dam 1.00x)
dam1                 5.00 L/s (dam 0.50x)
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
