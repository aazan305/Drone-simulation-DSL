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
import no.uio.aeroscript.antlr.AeroScriptParser.ActionContext;
import no.uio.aeroscript.ast.stmt.DescendAction;
import no.uio.aeroscript.ast.stmt.DockAction;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.ast.stmt.MoveAction;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
class TypeCheckTest {
  TypeCheck typechecker = new TypeCheck();
  private AeroScriptParser.ExpressionContext
  parseExpression(String expression) {
    AeroScriptLexer lexer =
        new AeroScriptLexer(CharStreams.fromString(expression));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    AeroScriptParser parser = new AeroScriptParser(tokens);
    return parser.expression();
  }
  private AeroScriptParser.ActionContext parseAction(String Action) {
    AeroScriptLexer lexer = new AeroScriptLexer(CharStreams.fromString(Action));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    AeroScriptParser parser = new AeroScriptParser(tokens);
    return parser.action();
  }

  @Test
  void TestPlusType() {

    AeroScriptParser.ExpressionContext expr = parseExpression("2+ 2");

    Object result = typechecker.visit(expr);
    assertEquals(TypeCheck.Type.NUM, result);
  }

  @Test
  void TestPlusTypeError() {

    AeroScriptParser.ExpressionContext expr = parseExpression("2+ point (2,1)");

    assertThrows(Error.class, () -> { typechecker.visit(expr); });
  }
  @Test
  void TestMinusWithRangeType() {

    AeroScriptParser.ExpressionContext expr =
        parseExpression("2 - random [1,10])");

    Object result = typechecker.visit(expr);
    assertEquals(TypeCheck.Type.NUM, result);
  }

  @Test
  void TestMinusTypeWithPointError() {

    AeroScriptParser.ExpressionContext expr =
        parseExpression("2 -  point (2,1)");

    assertThrows(Error.class, () -> { typechecker.visit(expr); });
  }
  @Test
  void TestMultiWithRangesType() {

    AeroScriptParser.ExpressionContext expr =
        parseExpression("random [1,19] * random [1,10])");

    Object result = typechecker.visit(expr);
    assertEquals(TypeCheck.Type.NUM, result);
  }

  @Test
  void TestActions() {
    ActionContext ascendAc = parseAction("ascend by 50");
    ActionContext descendAc = parseAction("descend by 100");
    ActionContext moveAc = parseAction("move by 1");
    ActionContext turnAc = parseAction("turn by 66");

    ActionContext[] actions = {ascendAc, descendAc, moveAc, turnAc};
    for (ActionContext ctx : actions) {
      assertNull(typechecker.visit(ctx));
    }
  }
}
