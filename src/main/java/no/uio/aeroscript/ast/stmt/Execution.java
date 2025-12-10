package no.uio.aeroscript.ast.stmt;

import java.util.ArrayList;
import java.util.List;
public class Execution {
  private String name;
  private boolean isStart;
  private String nextExecution;
  private ArrayList<Statement> statements;

  public Execution(String name, ArrayList<Statement> statements,
                   boolean isStart, String nextExecution) {
    this.name = name;
    this.isStart = isStart;
    this.nextExecution = nextExecution;
    this.statements = statements;
  }

  public String getName() { return name; }

  public Boolean isStart() { return isStart; }
  public String getNextExecution() { return nextExecution; }
  public ArrayList<Statement> getStatements() { return statements; }
}
