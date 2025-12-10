package no.uio.aeroscript.runtime;

import java.util.*;
import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.antlr.AeroScriptParser.NumExpContext;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.ast.stmt.*;
import no.uio.aeroscript.type.*;
import org.stringtemplate.v4.compiler.CodeGenerator.region_return;

/* For now, let us just ignore interrupts, will need to build
 * an interrupt lookup table and introduce a message queue later... */
public class Interpreter extends AeroScriptBaseVisitor<Object> {
  private HashMap<Memory, Object> heap;
  private Stack<Statement> stack;
  private LinkedHashMap<String, ArrayList<Statement>> methodTable =
      new LinkedHashMap<>();
  private LinkedHashMap<String, Execution> executionTable =
      new LinkedHashMap<>();
  private Map<String, Runnable> listeners = new HashMap<>();
  private Execution currentExec;
  public Interpreter(HashMap<Memory, Object> heap, Stack<Statement> stack) {
    this.heap = heap;
    this.stack = stack;

    Object obj = heap.get(Memory.VARIABLES);
    HashMap<String, Object> initialVars = null;
    if (obj instanceof HashMap) {

      initialVars = (HashMap<String, Object>)obj;
    }
    Point intialPosition;
    Float initalBatterYLevel;
    if (initialVars != null) {

      intialPosition = (Point)initialVars.get("initial position");
      initalBatterYLevel = (Float)initialVars.get("battery level");
    } else {
      intialPosition = new Point(0.0f, 0.0f);
      initalBatterYLevel = 100.0f;
    }
    heap.put(Memory.ALTITUDE, 0.0f);
    heap.put(Memory.BATTERY_LEVEL, initalBatterYLevel);
    heap.put(Memory.DISTANCE_TRAVELED, 0.0f);
    heap.put(Memory.INITIAL_MODE, null);
    heap.put(Memory.POSITION_X, intialPosition.getX());
    heap.put(Memory.POSITION_Y, intialPosition.getY());
  }
  public LinkedHashMap<String, ArrayList<Statement>> getTable() {
    return methodTable;
  }
  public LinkedHashMap<String, Execution> getExecutionTable() {
    return executionTable;
  }
  public Map<String, Runnable> getListeners() { return listeners; }

  @Override
  public Object visitPoint(AeroScriptParser.PointContext ctx) {
    Expression xNode = (Expression)visit(ctx.expression(0));
    Expression yNode = (Expression)visit(ctx.expression(1));
    float x = Float.parseFloat(xNode.evaluate().toString());
    float y = Float.parseFloat(yNode.evaluate().toString());
    return new Point(x, y);
  }

  @Override
  public Object visitRange(AeroScriptParser.RangeContext ctx) {
    Expression startNode = (Expression)visit(ctx.expression(0));
    Expression endNode = (Expression)visit(ctx.expression(1));
    float start = Float.parseFloat(startNode.evaluate().toString());
    float end = Float.parseFloat(endNode.evaluate().toString());
    return new Range(start, end);
  }

  /* Program = list of executions */
  @Override
  public ArrayList<Execution>
  visitProgram(AeroScriptParser.ProgramContext ctx) {
    ArrayList<Execution> executions = new ArrayList<Execution>();
    for (AeroScriptParser.ExecutionContext ExecCtx : ctx.execution()) {
      Execution exec = (Execution)visitExecution(ExecCtx);
      executions.add(exec);
      executionTable.put(exec.getName(), exec);
      // methodTable.put(exec.getName(), exec.getStatements());
    }
    registerListeneres(executions);
    // Implement the method:
    return executions;
  }

  private void registerListeneres(ArrayList<Execution> executions) {
    for (Execution exec : executions) {
      for (Statement stmt : exec.getStatements()) {
        if (stmt instanceof Reaction) {
          Reaction reaction = (Reaction)stmt;
          if ("message".equals(reaction.getEventType())) {
            String messageId = reaction.getMessageId();
            listeners.put(messageId,
                          ()
                              -> handleMessageEvent(
                                  messageId, reaction.getTargetExecution()));
          }
        }
      }
    }
  }

  private void handleMessageEvent(String messageId, String targetExecution) {
    System.out.println("Processing event -->" + messageId + " with target -->" +
                       targetExecution);

    // interrupted = true
    // interruptedTarget = targetExecution
    stack.clear();
    executeExecution(targetExecution);
  }
  @Override
  public Execution visitExecution(AeroScriptParser.ExecutionContext ctx) {

    String name = ctx.ID(0).toString();
    boolean isStart = ctx.getText().startsWith("->");
    String nextExecution = null;
    if (ctx.ID().size() > 1) {
      nextExecution = ctx.ID(1).toString();
    }
    ArrayList<Statement> statements = new ArrayList<Statement>();

    for (AeroScriptParser.StatementContext Sctx : ctx.statement()) {
      if (Sctx.action() != null) {
        Statement state = (Statement)visitStatement(Sctx);
        statements.add(state);
      } else if (Sctx.reaction() != null) {
        Statement state = (Statement)visitReaction(Sctx.reaction());
        statements.add(state);
      }
    }

    return new Execution(name, statements, isStart, nextExecution);
  }

  @Override
  public Reaction visitReaction(AeroScriptParser.ReactionContext ctx) {
    String targetExecution = ctx.ID().getText();

    if (ctx.event().getText().contains("obstacle")) {
      return new Reaction("obstacle", targetExecution, null);
    }
    if (ctx.event().getText().contains("low battery")) {
      return new Reaction("low battery", targetExecution, null);
    }
    if (ctx.event().getText().contains("message")) {
      String messageId = ctx.event().ID().getText();
      return new Reaction("message", targetExecution, messageId);
    }
    throw new RuntimeException("Unknown event type");
  }
  @Override
  public Statement visitStatement(AeroScriptParser.StatementContext ctx) {

    return (Statement)visitAction(ctx.action());
  }
  @Override
  public Action visitAction(AeroScriptParser.ActionContext ctx) {
    Expression time = new NumberExpression(0.0f);
    Expression speed = new NumberExpression(0.0f);
    if (ctx.getText().contains("for")) {
      time = (Expression)visit(ctx.expression());
    }
    if (ctx.getText().contains("at speed")) {
      speed = (Expression)visit(ctx.expression());
    }

    if (ctx.acDock() != null) {
      return visitDockAction(ctx.acDock(), time, speed);

    } else if (ctx.acMove() != null) {
      return visitMoveAction(ctx.acMove(), time, speed);

    } else if (ctx.acTurn() != null) {
      return visitTurnAction(ctx.acTurn(), time, speed);

    } else if (ctx.acAscend() != null) {
      return visitAscendAction(ctx.acAscend(), time, speed);

    } else if (ctx.acDescend() != null) {
      return visitDescendAction(ctx.acDescend(), time, speed);
    }
    throw new RuntimeException("Error with action");
  }

  public DockAction visitDockAction(AeroScriptParser.AcDockContext ctx,
                                    Expression time, Expression speed) {
    return new DockAction(time, speed);
  }
  public MoveAction visitMoveAction(AeroScriptParser.AcMoveContext ctx,
                                    Expression time, Expression speed) {
    Point point = null;
    Expression distance = null;
    if (ctx.getText().contains("point")) {

      point = (Point)visitPoint(ctx.point());
      return new MoveAction(time, speed, point);

    } else if (ctx.getText().contains("by")) {
      float value = Float.parseFloat(ctx.NUMBER().getText());
      distance = new NumberExpression(value);

      return new MoveAction(time, speed, distance);
    }
    throw new RuntimeException("Error with visiting MOVEACTION");
  }

  public TurnAction visitTurnAction(AeroScriptParser.AcTurnContext ctx,
                                    Expression time, Expression speed) {
    Expression angle = null;
    String direction = null;
    if (ctx.getText().contains("right")) {
      direction = "right";
    }
    if (ctx.getText().contains("left")) {
      direction = "left";
    }
    if (ctx.expression() != null) {
      angle = visitExpression(ctx.expression());
    }
    return new TurnAction(time, speed, angle, direction);
  }
  public AscendAction visitAscendAction(AeroScriptParser.AcAscendContext ctx,
                                        Expression time, Expression speed) {
    Expression ascension = null;

    if (ctx.expression() != null) {
      ascension = (Expression)visitExpression(ctx.expression());
    }
    return new AscendAction(time, speed, ascension);
  }
  public DescendAction visitDescendAction(AeroScriptParser.AcDescendContext ctx,
                                          Expression time, Expression speed) {
    Expression descension = null;

    if (ctx.expression() != null) {
      descension = visitExpression(ctx.expression());
    }
    return new DescendAction(time, speed, descension);
  }

  public void ExcuteProgram(ArrayList<Execution> executions) {
    for (Execution exec : executions) {
      if (exec.isStart()) {
        heap.put(Memory.INITIAL_MODE, exec.getName());
        executeExecution(exec.getName());
        break;
      }
    }
  }
  public void executeExecution(String name) {
    try {
      Execution exec = executionTable.get(name);

      if (exec == null) {
        System.out.println("Error with execution --> like null");
        return;
      }

      currentExec = exec;
      // interrupted= false;

      System.out.println("Enter execution with first as " + name);
      for (int i = exec.getStatements().size() - 1; i >= 0; i--) {

        stack.push(exec.getStatements().get(i));
      }

      while (!stack.isEmpty()) {
        Float batteryLevel = (Float)heap.get(Memory.BATTERY_LEVEL);

        if (batteryLevel < 20.0f) {
          Reaction lowBatteryReaction = findReaction("low battery");
          if (lowBatteryReaction != null) {
            System.out.println("> Low battery detected! Transitioning to " +
                               lowBatteryReaction.getTargetExecution());
            stack.clear();
            executeExecution(lowBatteryReaction.getTargetExecution());
            return;
          } else if (batteryLevel <= 0.0f) {
            stack.clear();
            System.out.println("> BATTERY IS EMPTY! EXITING!");
            System.exit(0);
          }
        }
        Reaction obstacleReaction = findReaction("obstacle");
        if (obstacleReaction != null) {
          System.out.println("> reacting to obstacle! Transitioning to " +
                             obstacleReaction.getTargetExecution());
          stack.clear();
          executeExecution(obstacleReaction.getTargetExecution());
          return;
        }
        Statement stmt = stack.pop();
        if (stmt instanceof Reaction) {

          continue;
        }
        stmt.execute(heap);
      }
      if (exec.getNextExecution() != null) {
        executeExecution(exec.getNextExecution());
      }
    }

    catch (Exception e) {
      System.out.println("> Exception inside executeExecution(" + name +
                         "): " + e);
      e.printStackTrace(System.out);
    }
  }
  public Reaction findReaction(String EvenType) {

    if (currentExec == null)
      return null;

    for (Statement stmt : currentExec.getStatements()) {
      if (stmt instanceof Reaction) {
        Reaction reaction = (Reaction)stmt;
        if (EvenType.equals(reaction.getEventType())) {

          return reaction;
        }
      }
    }
    return null;
  }
  public Expression visitExpression(AeroScriptParser.ExpressionContext ctx) {
    if (ctx instanceof AeroScriptParser.PlusExpContext pex) {
      return visitPlusExp(pex);
    } else if (ctx instanceof AeroScriptParser.MinusExpContext pex) {
      return visitMinusExp(pex);
    } else if (ctx instanceof AeroScriptParser.TimesExpContext pex) {
      return visitTimesExp(pex);
    } else if (ctx instanceof AeroScriptParser.NumExpContext pex) {
      return visitNumExp(pex);
    } else if (ctx instanceof AeroScriptParser.NegExpContext pex) {
      return visitNegExp(pex);
    } else if (ctx instanceof AeroScriptParser.ParentExpContext pex) {
      return visitParentExp(pex);
    } else if (ctx instanceof AeroScriptParser.RangeExpContext pex) {
      return visitRangeExp(pex);
    } else if (ctx instanceof AeroScriptParser.PointExpContext pex) {
      return visitPointExp(pex);
    } else {
      return new NumberExpression(0.0f);
    }
  }
  /* Expressions */

  @Override
  public Expression visitPlusExp(AeroScriptParser.PlusExpContext ctx) {
    Expression left = visitExpression(ctx.left);
    Expression right = visitExpression(ctx.right);
    return new OperationExpression("SUM", left, right);
  }
  @Override
  public Expression visitMinusExp(AeroScriptParser.MinusExpContext ctx) {
    Expression left = visitExpression(ctx.left);
    Expression right = visitExpression(ctx.right);
    return new OperationExpression("SUB", left, right);
  }
  @Override
  public Expression visitTimesExp(AeroScriptParser.TimesExpContext ctx) {
    Expression left = visitExpression(ctx.left);
    Expression right = visitExpression(ctx.right);
    return new OperationExpression("MUL", left, right);
  }
  @Override
  public Expression visitNumExp(AeroScriptParser.NumExpContext ctx) {
    return new NumberExpression(Float.parseFloat(ctx.NUMBER().getText()));
  }
  @Override
  public Expression visitNegExp(AeroScriptParser.NegExpContext ctx) {
    return new NegNumberExpression(visitExpression(ctx.expression()));
  }
  @Override
  public Expression visitParentExp(AeroScriptParser.ParentExpContext ctx) {
    return visitExpression(ctx.expression());
  }
  @Override
  public Expression visitPointExp(AeroScriptParser.PointExpContext ctx) {
    Expression left = visitExpression(ctx.point().left);
    Expression right = visitExpression(ctx.point().right);
    return new PointExpression(left, right);
  }
  @Override
  public Expression visitRangeExp(AeroScriptParser.RangeExpContext ctx) {
    Expression left = visitExpression(ctx.range().left);
    Expression right = visitExpression(ctx.range().right);
    return new RangeExpression(left, right);
  }
}
