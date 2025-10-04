## Documentation 

- (./waterflow-parser/doc.pdf)

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
