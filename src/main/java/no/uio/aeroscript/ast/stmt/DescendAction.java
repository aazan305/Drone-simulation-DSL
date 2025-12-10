package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.type.Memory;

public class DescendAction extends Action {
  private Expression time;
  private Expression speed;
  private Expression descension;

  public DescendAction(Expression time, Expression speed,
                       Expression descension) {
    this.time = time;
    this.speed = speed;
    this.descension = descension;
  }

  @Override
  public void execute(HashMap<Memory, Object> heap) {

    float batterylevel = (Float)heap.get(Memory.BATTERY_LEVEL);
    float timeExp = (Float)time.evaluate();
    float speedExp = (Float)speed.evaluate();
    // float descensionExp = Float.parseFloat(descension.toString());
    float oldAltidtude = (Float)heap.get(Memory.ALTITUDE);
    float oldDtistance = (Float)heap.get(Memory.DISTANCE_TRAVELED);
    if (descension != null) {
      float descensionExp = ((Number)this.descension.evaluate()).floatValue();

      System.out.println("Desceinding by " + descensionExp + "at SPEED :" +
                         speedExp + "for " + timeExp + " seconds.");
      heap.put(Memory.ALTITUDE, oldAltidtude - descensionExp);
      heap.put(Memory.DISTANCE_TRAVELED, oldDtistance + descensionExp);
      float batteryDepletion =
          ((descensionExp * 0.2f) + (timeExp * 0.0f) + (speedExp * 1));
      if (batterylevel - batteryDepletion > 0f) {
        heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepletion);
      } else {
        heap.put(Memory.BATTERY_LEVEL, 0.0f);
      }
    } else {
      System.out.println("Descending to ground ");
      heap.put(Memory.ALTITUDE, 0.0f);

      heap.put(Memory.DISTANCE_TRAVELED, oldDtistance + oldAltidtude);

      float batteryDepletion =
          ((oldAltidtude * 0.2f) + (timeExp * 0.0f) + (speedExp * 1));
      if (batterylevel - batteryDepletion > 0f) {
        heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepletion);
      } else {
        heap.put(Memory.BATTERY_LEVEL, 0.0f);
      }
    }
  }
}
