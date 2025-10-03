import java.util.List;

public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  public String print(Stmt stmt) {
    return stmt.accept(this);
  }

  public String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitRiverStmt(Stmt.River stmt) {
    if (stmt.flowRate != null) {
      return "(river " + stmt.name.lexeme + " = " + print(stmt.flowRate) + ")";
    }
    return "(river " + stmt.name.lexeme + ")";
  }

  @Override
  public String visitOutputStmt(Stmt.Output stmt) {
    return "(output " + stmt.riverName.lexeme + ")";
  }

  @Override
  public String visitCombineStmt(Stmt.Combine stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(combine ").append(stmt.name.lexeme).append(" = ");
    for (int i = 0; i < stmt.sources.size(); i++) {
      if (i > 0) builder.append(" + ");
      builder.append(stmt.sources.get(i).lexeme);
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitFlowStmt(Stmt.Flow stmt) {
    return "(flow " + stmt.from.lexeme + " -> " + stmt.to.lexeme + ")";
  }

  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(block ");
    for (Stmt statement : stmt.statements) {
      builder.append(statement.accept(this));
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return parenthesize(";", stmt.expression);
  }

  @Override
  public String visitPrintStmt(Stmt.Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer != null) {
      return parenthesize2("var " + stmt.name.lexeme, stmt.initializer);
    }
    return parenthesize("var " + stmt.name.lexeme);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if (expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return expr.name.lexeme;
  }

  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }

  private String parenthesize2(String name, Object... parts) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    transform(builder, parts);
    builder.append(")");

    return builder.toString();
  }

  private void transform(StringBuilder builder, Object[] parts) {
    for (Object part : parts) {
      builder.append(" ");
      if (part instanceof Expr) {
        builder.append(((Expr)part).accept(this));
      } else if (part instanceof Stmt) {
        builder.append(((Stmt)part).accept(this));
      } else if (part instanceof Token) {
        builder.append(((Token) part).lexeme);
      } else if (part instanceof List) {
        transform(builder, ((List) part).toArray());
      } else {
        builder.append(part);
      }
    }
  }
}