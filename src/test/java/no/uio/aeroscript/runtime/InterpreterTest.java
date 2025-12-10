package no.uio.aeroscript.runtime;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Stack;
import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.stmt.DescendAction;
import no.uio.aeroscript.ast.stmt.DockAction;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.ast.stmt.MoveAction;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;
class InterpreterTest {
  private HashMap<Memory, Object> heap;
  private Stack<Statement> stack;

  private void initInterpreter() {
    this.heap = new HashMap<>();
    this.stack = new Stack<>();
    HashMap<Memory, HashMap<String, Object>> variables = new HashMap<>();
    variables.put(Memory.VARIABLES, new HashMap<>());
    HashMap<String, Object> vars = variables.get(Memory.VARIABLES);

    float batteryLevel = 100;
    int initialZ = 0;
    Point initialPosition = new Point(0, 0);

    vars.put("initial position", initialPosition);
    vars.put("current position", initialPosition);
    vars.put("altitude", initialZ);
    vars.put("initial battery level", batteryLevel);
    vars.put("battery level", batteryLevel);
    vars.put("battery low", false);
    vars.put("distance travelled", 0.0f);
    vars.put("initial execution", null);

    heap.put(Memory.VARIABLES, vars);
  }

  private AeroScriptParser.ExpressionContext
  parseExpression(String expression) {
    AeroScriptLexer lexer =
        new AeroScriptLexer(CharStreams.fromString(expression));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    AeroScriptParser parser = new AeroScriptParser(tokens);
    return parser.expression();
  }
  @Test
  void getFirstExecution() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);

    Statement dummyStmt = new Statement() {
      @Override
      public void execute(HashMap<Memory, Object> heap) {}
    };

    ArrayList<Statement> testStmt = new ArrayList<>();
    testStmt.add(dummyStmt);

    Execution first = new Execution("start", testStmt, true, "next");
    Execution second = new Execution("next", testStmt, false, null);

    ArrayList<Execution> testList = new ArrayList<>();
    testList.add(first);
    testList.add(second);
    interpreter.getExecutionTable().put(first.getName(), first);
    interpreter.getExecutionTable().put(second.getName(), second);
    interpreter.ExcuteProgram(testList);

    LinkedHashMap<String, Execution> table = interpreter.getExecutionTable();

    Iterator<String> it = table.keySet().iterator();
    assertEquals("start", it.next());
    assertEquals("next", it.next());
  }

  @Test
  void getPosition() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);
    float x = (Float)heap.get(Memory.POSITION_X);
    float y = (Float)heap.get(Memory.POSITION_Y);

    assertEquals(0.0f, x);
    assertEquals(0.0f, y);
    // the correct position
  }

  @Test
  void getDistanceTravelled() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);
    heap.put(Memory.DISTANCE_TRAVELED, 100.0f);
    assertEquals(
        100.0f,
        heap.get(Memory.DISTANCE_TRAVELED)); // Implement the test, ensure I get
                                             //
  }

  @Test
  void getBatteryLevel() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);
    float battery = (Float)heap.get(Memory.BATTERY_LEVEL);
    assertEquals(100.0f, battery);
  }

  @Test
  void visitProgram() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);

    // Implement the test, read a file and parse it, then ensure you have the
    // first execution, and that the number of exeuctions is correct (in the
    // program.aero file there are 9 executions)

    try {

      String content = new String(
          Files.readAllBytes(Paths.get("src/test/resources/program.aero")));
      AeroScriptLexer lexer =
          new AeroScriptLexer(CharStreams.fromString(content));
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      AeroScriptParser parser = new AeroScriptParser(tokens);

      ArrayList<Execution> executions =
          interpreter.visitProgram(parser.program());

      assertEquals(5, executions.size(), "Should have 5 executions");

    } catch (Exception e) {
      fail("Failed to read or parse program file: " + e.getMessage());
    }
  }

  @Test
  void visitExpression() {
    initInterpreter();
    Interpreter interpreter = new Interpreter(this.heap, this.stack);

    assertEquals(5.0f, Float.parseFloat(
                           interpreter.visitExpression(parseExpression("2 + 3"))
                               .evaluate()
                               .toString()));
    assertEquals(
        -1.0f,
        Float.parseFloat(interpreter.visitExpression(parseExpression("2 - 3"))
                             .evaluate()
                             .toString()));
    assertEquals(6.0f, Float.parseFloat(
                           interpreter.visitExpression(parseExpression("2 * 3"))
                               .evaluate()
                               .toString()));
    assertEquals(-1, Float.parseFloat(
                         interpreter.visitExpression(parseExpression("-- 1"))
                             .evaluate()
                             .toString()));
  }
}
