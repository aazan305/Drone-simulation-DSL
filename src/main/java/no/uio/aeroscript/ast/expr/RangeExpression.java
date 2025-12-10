package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.Range;

public class RangeExpression extends Expression {
    private final Expression left;
    private final Expression right;

    public RangeExpression(Expression l, Expression r){left = l; right = r;}

    @Override
    public void print(){
        System.out.print("RangeNode[");
        this.left.print();
        System.out.print(", ");
        this.right.print();
        System.out.print("]\n");
    }

    @Override
    public Float evaluate(){
        Object lval = this.left.evaluate();
        Object rval = this.right.evaluate();
        if (lval instanceof Float f1 && rval instanceof Float f2) {
            return new Range(f1,f2).getRandomNumber();
        }
        else throw new IllegalArgumentException("Invalid arguments to arithmetic operation");
    }
}
