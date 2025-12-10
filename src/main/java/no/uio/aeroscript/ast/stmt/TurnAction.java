package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.type.Memory;

public class TurnAction extends Action {
  private Expression time;
  private Expression speed;
  private Expression angle;
  private String direction;

  public TurnAction(Expression time, Expression speed, Expression angle,
                    String direction) {
    this.time = time;
    this.speed = speed;
    this.angle = angle;
    this.direction = direction;
  }

  @Override
  public void execute(HashMap<Memory, Object> heap) {

    float batterylevel = (Float)heap.get(Memory.BATTERY_LEVEL);
    float timeExp = (Float)time.evaluate();
    float speedExp = (Float)speed.evaluate();
    float angleExp = ((Number)angle.evaluate()).floatValue();

    if (direction == "right") {
      System.out.println("Turining right by :" + angleExp + " degrees"
                         + "at SPEED :" + speedExp + "for " + timeExp +
                         " seconds.");

    } else {
      System.out.println("Turning left by :" + angleExp + " degrees"
                         + "at SPEED :" + speedExp + "for " + timeExp +
                         " seconds.");
    }
    float batteryDepletion =
        ((angleExp * 0.3f) + (timeExp * 0.1f) + (speedExp * 1f));
    if (batterylevel - batteryDepletion > 0f) {
      heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepletion);
    } else {
      heap.put(Memory.BATTERY_LEVEL, 0.0f);
    }
  }
}
