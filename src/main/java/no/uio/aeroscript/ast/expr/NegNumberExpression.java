package no.uio.aeroscript.ast.expr;

public class NegNumberExpression extends Expression {
    // Implement the NumberNode class to handle numbers
    private final Expression val;
    public NegNumberExpression(Expression val) { this.val = val;}

    @Override
    public void print(){
        System.out.print("NegNumberNode(");
        val.print();
        System.out.print(")\n");
    }

    @Override
    public Float evaluate(){
        Object tmp = this.val.evaluate();
        if (tmp instanceof Float val) {
            return - val ;
        } else {
            throw new IllegalArgumentException("Wanted a Number, got a Node " + tmp);
        }
    }
}
