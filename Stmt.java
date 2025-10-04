import java.util.List;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);
    R visitExpressionStmt(Expression stmt);
    R visitPrintStmt(Print stmt);
    R visitVarStmt(Var stmt);
    R visitRiverStmt(River stmt);
    R visitOutputStmt(Output stmt);
    R visitCombineStmt(Combine stmt);
    R visitFlowStmt(Flow stmt);
  }

  public static class Block extends Stmt {
    public Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    public final List<Stmt> statements;
  }

  public static class Expression extends Stmt {
    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    public final Expr expression;
  }

  public static class Print extends Stmt {
    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    public final Expr expression;
  }

  public static class Var extends Stmt {
    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    public final Token name;
    public final Expr initializer;
  }

  // Wflow language statements
  public static class River extends Stmt {
    public River(Token name, Expr flowRate) {
      this.name = name;
      this.flowRate = flowRate;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitRiverStmt(this);
    }

    public final Token name;
    public final Expr flowRate;
  }

  public static class Output extends Stmt {
    public Output(Token riverName) {
      this.riverName = riverName;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitOutputStmt(this);
    }

    public final Token riverName;
  }

  public static class Combine extends Stmt {
    public Combine(Token name, List<Token> sources) {
      this.name = name;
      this.sources = sources;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitCombineStmt(this);
    }

    public final Token name;
    public final List<Token> sources;
  }

  public static class Flow extends Stmt {
    public Flow(Token from, Token to) {
      this.from = from;
      this.to = to;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFlowStmt(this);
    }

    public final Token from;
    public final Token to;
  }

  public abstract <R> R accept(Visitor<R> visitor);
}