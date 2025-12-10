package no.uio.aeroscript.ast.expr;

import no.uio.aeroscript.type.Point;

public class OperationExpression extends Expression {
  private final String operation;
  private final Expression left;
  private final Expression right;

  public OperationExpression(String operation, Expression left,
                             Expression right) {
    this.operation = operation;
    this.left = left;
    this.right = right;
  }

  @Override
  public void print() {
    System.out.print("OperationNode(");
    this.left.print();
    System.out.print(" " + this.operation + " ");
    this.right.print();
    System.out.print(")\n");
  }

  @Override
  public Object evaluate() {
    // Implement method to evaluate the various expressions
    Object val1 = left.evaluate();
    Object val2 = right.evaluate();
    if (val1 instanceof Float f1 && val2 instanceof Float f2) {
      return switch (operation) {
        case "SUM" -> f1 + f2;
        case "SUB" -> f1 - f2;
        case "MUL" -> f1 *f2;
        case "DIV" -> f1 / f2;
        case "MOD" -> f1 % f2;
        default ->
          throw new IllegalArgumentException("Invalid operation: " + operation);
      };
    }
    if (val1 instanceof Point lPoint && val2 instanceof Point rPoint) {
      switch (operation) {
      case "SUM":
        return new Point(lPoint.getX() + rPoint.getX(),
                         lPoint.getY() + rPoint.getY());

      case "SUB":
        return new Point(lPoint.getX() - rPoint.getX(),
                         lPoint.getY() - rPoint.getY());
      case "MUL":
        return new Point(lPoint.getX() * rPoint.getX(),
                         lPoint.getY() * rPoint.getY());
      case "DIV":
        return new Point(lPoint.getX() / rPoint.getX(),
                         lPoint.getY() / rPoint.getY());
      default:
        throw new IllegalArgumentException("error with point operation");
      }
    }
    throw new IllegalArgumentException("error doing operation");
  }
}
