package no.uio.aeroscript.runtime;

import java.util.*;
import no.uio.aeroscript.antlr.AeroScriptBaseVisitor;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.antlr.AeroScriptParser.ExpressionContext;
import no.uio.aeroscript.antlr.AeroScriptParser.NumExpContext;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.ast.stmt.*;
import no.uio.aeroscript.type.*;
import org.stringtemplate.v4.compiler.CodeGenerator.region_return;

/* For now, let us just ignore interrupts, will need to build
 * an interrupt lookup table and introduce a message queue later... */
public class TypeCheck extends AeroScriptBaseVisitor<Object> {

  public TypeCheck() {}
  enum Type {
    NUM,
    POINT,
    RANGE,
  }

  @Override
  public Object visitProgram(AeroScriptParser.ProgramContext ctx) {
    for (AeroScriptParser.ExecutionContext ectx : ctx.execution()) {
      System.out.println("visiting for typechecker");
      visitExecution(ectx);
    }
    System.out.println("Visit completed");
    return null;
  }
  @Override
  public Object visitExecution(AeroScriptParser.ExecutionContext ectx) {

    for (AeroScriptParser.StatementContext sctx : ectx.statement()) {
      visitStatement(sctx);
    }
    return null;
  }
  @Override
  public Object visitStatement(AeroScriptParser.StatementContext sctx) {
    if (sctx.action() != null) {
      visitAction(sctx.action());
    }
    return null;
  }
  @Override
  public Object visitAction(AeroScriptParser.ActionContext actx) {

    if (actx.getText().contains("for")) {
      Type timeType = (Type)visit(actx.expression());
      if (timeType != Type.NUM) {
        throw new Error("for error");
      }
    }
    if (actx.getText().contains("at speed")) {
      Type speedType = (Type)visit(actx.expression());
      System.out.println("found AT SPEED WITH expression TYPE as" + speedType +
                         " <- euqal NUM??");
      if (speedType != Type.NUM) {
        throw new Error("at speed errror at" + actx.expression().toString());
      }
    }

    if (actx.acDock() != null) {
      visitAcDock(actx.acDock());
    } else if (actx.acAscend() != null) {
      visitAcAscend(actx.acAscend());
    } else if (actx.acDescend() != null) {
      visitAcDescend(actx.acDescend());
    } else if (actx.acMove() != null) {
      visitAcMove(actx.acMove());
    } else if (actx.acTurn() != null) {
      visitAcTurn(actx.acTurn());
    }
    return null;
  }
  @Override
  public Object visitAcDock(AeroScriptParser.AcDockContext ctx) {

    return null;
  }
  @Override
  public Object visitAcMove(AeroScriptParser.AcMoveContext ctx) {
    Type movepoint = null;
    if (ctx.getText().contains("point")) {
      movepoint = (Type)visitPoint(ctx.point());
      if (movepoint != Type.POINT) {
        throw new Error("error with point at" + ctx.point().toString());
      }
    }
    System.out.println("visited a acMove and found a Type " + movepoint +
                       " from context " + ctx.getText());
    return null;
  }

  @Override
  public Object visitAcTurn(AeroScriptParser.AcTurnContext ctx) {
    Type angelType = (Type)visit(ctx.expression());
    if (angelType != Type.NUM) {
      throw new Error("error with angle at" + ctx.expression().toString());
    }
    System.out.println("visited a acTurn");
    return null;
  }
  @Override
  public Object visitAcAscend(AeroScriptParser.AcAscendContext ctx) {
    Type ascendType = (Type)visit(ctx.expression());
    if (ascendType != Type.NUM) {
      throw new Error("error with ascend at" + ctx.expression().toString());
    }
    System.out.println("visited a acAscend");
    return null;
  }
  @Override
  public Object visitAcDescend(AeroScriptParser.AcDescendContext ctx) {
    if (ctx.getText().contains("descend to ground")) {
      return null;
    }
    Type descendType = (Type)visit(ctx.expression());
    if (descendType != Type.NUM) {

      throw new Error("error with descend at" + ctx.expression().toString());
    }
    System.out.println("visited a acDescend");
    return null;
  }

  @Override
  public Object visitPoint(AeroScriptParser.PointContext ctx) {
    Type xType = (Type)visit(ctx.expression(0));
    Type yType = (Type)visit(ctx.expression(1));

    if (xType != Type.NUM) {
      throw new Error("error with point at " + ctx.toString());
    }
    if (yType != Type.NUM) {
      throw new Error("error with point at " + ctx.toString());
    }
    System.out.println("visited a Point and returned type point");
    return Type.POINT;
  }

  @Override
  public Object visitRange(AeroScriptParser.RangeContext ctx) {
    Type type1 = (Type)visit(ctx.expression(0));
    Type type2 = (Type)visit(ctx.expression(1));

    if (type1 != Type.NUM) {
      throw new Error("Range start must be Num, got " + type1);
    }
    if (type2 != Type.NUM) {
      throw new Error("Range end must be Num, got " + type2);
    }

    return Type.NUM;
  }

  @Override
  public Type visitNumExp(AeroScriptParser.NumExpContext ctx) {
    System.out.println("visited a NUMexp " + ctx.getText());
    return Type.NUM;
  }

  @Override
  public Type visitNegExp(AeroScriptParser.NegExpContext ctx) {
    Type negType = (Type)visit(ctx.expression());
    if (negType != Type.NUM) {
      throw new Error("error with type Neg at" + ctx.getText());
    }
    return Type.NUM;
  }

  @Override
  public Type visitPlusExp(AeroScriptParser.PlusExpContext ctx) {
    Type plusleftType = (Type)visit(ctx.left);
    Type plusrightType = (Type)visit(ctx.right);
    /*
    if (plusleftType != Type.NUM && plusrightType != Type.NUM) {
      throw new Error("error with addition at" + ctx.getText());
    }*/
    System.out.println("visiting a plus Exp " + ctx.getText());

    if (plusleftType.equals(Type.NUM) && plusrightType.equals(Type.NUM)) {
      return Type.NUM;
    }
    if (plusleftType.equals(Type.POINT) && plusrightType.equals(Type.POINT)) {
      return Type.POINT;
    }
    throw new Error("Error with addition");
  }
  @Override
  public Type visitMinusExp(AeroScriptParser.MinusExpContext ctx) {
    Type minusleftType = (Type)visit(ctx.left);
    Type minusrightType = (Type)visit(ctx.right);
    if (minusleftType.equals(Type.NUM) && minusrightType.equals(Type.NUM)) {
      return Type.NUM;
    }
    if (minusleftType.equals(Type.POINT) && minusrightType.equals(Type.POINT)) {
      return Type.POINT;
    }
    throw new Error("Error with minus");
  }
  @Override
  public Object visitParentExp(AeroScriptParser.ParentExpContext ctx) {
    // Type expressionType = (Type)visit(ctx.expression());
    System.out.println("Found a PerentExp "
                       + " from " + ctx.getText());
    return visit(ctx.expression());
  }

  @Override
  public Type visitTimesExp(AeroScriptParser.TimesExpContext ctx) {
    Type timesleftType = (Type)visit(ctx.left);
    Type timesrightType = (Type)visit(ctx.right);
    if (timesleftType.equals(Type.NUM) && timesrightType.equals(Type.NUM)) {
      return Type.NUM;
    }
    if (timesleftType.equals(Type.POINT) && timesrightType.equals(Type.POINT)) {
      return Type.POINT;
    }
    throw new Error("Error with times");
  }

  @Override
  public Type visitPointExp(AeroScriptParser.PointExpContext ctx) {
    Type pointleftType = (Type)visit(ctx.point().left);
    Type pointrightType = (Type)visit(ctx.point().right);
    if (pointleftType != Type.NUM && pointrightType != Type.NUM) {
      throw new Error("error with aPoint at" + ctx.getText());
    }
    System.out.println("visited a point -> " + ctx.getText());
    return Type.POINT;
  }
  @Override
  public Type visitRangeExp(AeroScriptParser.RangeExpContext ctx) {
    if (ctx.range() != null) {

      return (Type)visitRange(ctx.range());
    }
    return Type.NUM;
  }
}
