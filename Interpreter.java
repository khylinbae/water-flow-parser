import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    private final Environment globals = new Environment();
    private Environment environment = globals;
    private final Map<String, RiverState> rivers;
    private final double rainfallMm;
    private final int dayNumber;
    private final int totalDays;

    public Interpreter(double rainfallMm, int dayNumber, int totalDays, Map<String, RiverState> sharedRivers) {
        this.rainfallMm = rainfallMm;
        this.dayNumber = dayNumber;
        this.totalDays = totalDays;
        this.rivers = sharedRivers == null ? new LinkedHashMap<>() : sharedRivers;
        globals.define("rainfall", rainfallMm);
        globals.define("day", (double) dayNumber);
        resetRiversForDay();
    }

    public void interpret(List<Stmt> statements) {
        try {
            System.out.printf("\n=== Day %d of %d with %.1f mm rainfall ===%n", dayNumber, totalDays, rainfallMm);
            for (Stmt statement : statements) {
                execute(statement);
            }
            printRiverSummary();
        } catch (RuntimeError error) {
            Lox.runtimeError(error);
        }
    }

    private void resetRiversForDay() {
        for (RiverState state : rivers.values()) {
            state.startDay();
        }
    }

    private void execute(Stmt stmt) {
        if (stmt == null) return;
        stmt.accept(this);
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }
        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Void visitRiverStmt(Stmt.River stmt) {
        double flow = stmt.flowRate == null ? rainfallMm : requireNumber(stmt.flowRate, stmt.name);
        RiverState state = getRiverState(stmt.name.lexeme);
        state.setIntrinsicFlow(flow);
        rivers.putIfAbsent(stmt.name.lexeme, state);
        return null;
    }

    @Override
    public Void visitOutputStmt(Stmt.Output stmt) {
        double flow = getRiverFlow(stmt.riverName);
        System.out.printf("%s flow: %.2f L/s%n", stmt.riverName.lexeme, flow);
        return null;
    }

    @Override
    public Void visitCombineStmt(Stmt.Combine stmt) {
        double total = 0.0;
        for (Token source : stmt.sources) {
            total += getRiverFlow(source);
        }
        RiverState state = getRiverState(stmt.name.lexeme);
        state.setIntrinsicFlow(total);
        rivers.putIfAbsent(stmt.name.lexeme, state);
        return null;
    }

    @Override
    public Void visitFlowStmt(Stmt.Flow stmt) {
        double transfer = getRiverFlow(stmt.from);
        RiverState target = getRiverState(stmt.to.lexeme);
        target.addIncomingFlow(transfer);
        rivers.putIfAbsent(stmt.to.lexeme, target);
        return null;
    }

    @Override
    public Void visitDamStmt(Stmt.Dam stmt) {
        RiverState state = getRiverState(stmt.riverName.lexeme);
        double inflow = state.inflow();
        double factor;
        switch (stmt.mode.type) {
            case OPEN:
                factor = 1.0;
                break;
            case CLOSE:
                factor = 0.0;
                break;
            case ADJUST:
                factor = evaluateDamAdjustment(stmt.adjustment, inflow, state.damLevel, stmt);
                break;
            default:
                throw new RuntimeError(stmt.mode, "Unsupported dam mode.");
        }
        if (factor < 0) {
            throw new RuntimeError(stmt.mode, "Dam factor cannot be negative.");
        }
        state.setDamFactor(factor);
        double outflow = inflow * factor;
        state.updateDamLevel(rainfallMm + inflow - outflow);
        rivers.putIfAbsent(stmt.riverName.lexeme, state);
        return null;
    }

    private double evaluateDamAdjustment(Expr adjustment, double inflow, double damLevel, Stmt.Dam stmt) {
        Environment previous = environment;
        Environment damEnv = new Environment(environment);
        damEnv.define("inflow", inflow);
        damEnv.define("damLevel", damLevel);
        environment = damEnv;
        try {
            return requireNumber(adjustment, stmt.mode);
        } finally {
            environment = previous;
        }
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }
                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
        }

        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        if (rivers.containsKey(expr.name.lexeme)) {
            return getRiverFlow(expr.name);
        }
        return environment.get(expr.name);
    }

    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    private double getRiverFlow(Token token) {
        return getRiverFlow(token.lexeme);
    }

    private double getRiverFlow(String name) {
        RiverState state = getRiverState(name);
        rivers.putIfAbsent(name, state);
        return state.currentFlow();
    }

    private RiverState getRiverState(String name) {
        return rivers.computeIfAbsent(name, key -> new RiverState());
    }

    private double requireNumber(Expr expr, Token context) {
        Object value = evaluate(expr);
        if (!(value instanceof Double)) {
            throw new RuntimeError(context, "Expected number.");
        }
        return (double) value;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;
        return true;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;
        return a.equals(b);
    }

    private String stringify(Object object) {
        if (object == null) return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    private void printRiverSummary() {
        if (rivers.isEmpty()) {
            System.out.println("No river flows computed.");
            return;
        }
        System.out.println();
        System.out.printf("== River flows after day %d ==%n", dayNumber);
        for (Map.Entry<String, RiverState> entry : rivers.entrySet()) {
            RiverState state = entry.getValue();
            System.out.printf("%-20s %.2f L/s (dam %.2fx, level %.2f m3)%n",
                    entry.getKey(), state.currentFlow(), state.damFactor, state.damLevel);
        }
    }

    public static class RiverState {
        private double intrinsicFlow = 0.0;
        private double incomingFlow = 0.0;
        private double damFactor = 1.0;
        private double damLevel = 0.0;

        void startDay() {
            intrinsicFlow = 0.0;
            incomingFlow = 0.0;
            damFactor = 1.0;
        }

        void setIntrinsicFlow(double flow) {
            intrinsicFlow = flow;
        }

        void addIncomingFlow(double flow) {
            incomingFlow += flow;
        }

        void setDamFactor(double factor) {
            damFactor = factor;
        }

        void updateDamLevel(double delta) {
            damLevel = Math.max(0.0, damLevel + delta);
        }

        double inflow() {
            return intrinsicFlow + incomingFlow;
        }

        double currentFlow() {
            return inflow() * damFactor;
        }
    }
}
