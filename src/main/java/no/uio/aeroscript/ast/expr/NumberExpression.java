package no.uio.aeroscript.ast.expr;

public class NumberExpression extends Expression {
    // Implement the NumberNode class to handle numbers
    private final Float number;
    public NumberExpression(Float number){ this.number = number;}

    public void print(){
        System.out.print("NumberNode(" + number.toString() + ")\n");
    }

    @Override
    public Float evaluate(){
        return this.number;
    }
}

