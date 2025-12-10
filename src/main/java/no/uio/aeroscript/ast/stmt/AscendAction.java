package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.ast.expr.NumberExpression;
import no.uio.aeroscript.type.Memory;

public class AscendAction extends Action {
  private Expression time;
  private Expression speed;
  private Expression ascension;

  public AscendAction(Expression time, Expression speed, Expression ascension) {
    this.time = time;
    this.speed = speed;
    this.ascension = ascension;
  }

  @Override
  public void execute(HashMap<Memory, Object> heap) {

    float batterylevel = (Float)heap.get(Memory.BATTERY_LEVEL);
    float timeExp = (Float)time.evaluate();
    float speedExp = (Float)speed.evaluate();
    float ascensionExp = (Float)ascension.evaluate();
    float oldAltidtude = (Float)heap.get(Memory.ALTITUDE);
    float oldDtistance = (Float)heap.get(Memory.DISTANCE_TRAVELED);

    System.out.println("Asceding by :" + ascensionExp + "at SPEED :" +
                       speedExp + "for " + timeExp + " seconds.");
    heap.put(Memory.ALTITUDE, oldAltidtude + ascensionExp);

    heap.put(Memory.DISTANCE_TRAVELED, oldDtistance + ascensionExp);

    float batteryDepletion =
        ((ascensionExp * 0.6f) + (timeExp * 0.0f) + (speedExp * 1));
    if (batterylevel - batteryDepletion > 0f) {
      heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepletion);
    } else {
      heap.put(Memory.BATTERY_LEVEL, 0.0f);
    }
  }
}
