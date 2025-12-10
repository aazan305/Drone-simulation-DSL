package no.uio.aeroscript.ast.expr;


public abstract class Expression {
    public abstract void print();
    public abstract Object evaluate();
}
