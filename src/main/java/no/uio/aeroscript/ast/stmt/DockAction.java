package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.type.Memory;

public class DockAction extends Action {
  private Expression time;
  private Expression speed;

  public DockAction(Expression time, Expression speed) {
    this.time = time;
    this.speed = speed;
  }
  @Override
  public void execute(HashMap<Memory, Object> heap) {

    float timeValue = (Float)time.evaluate();
    float speedValue = (Float)speed.evaluate();
    System.out.println("Returning to Dock"
                       + "at SPEED :" + speedValue + "for " + timeValue +
                       " seconds.");
    float altitude = (Float)heap.get(Memory.ALTITUDE);
    float distance = (Float)heap.get(Memory.DISTANCE_TRAVELED);
    float currentBattery = (Float)heap.get(Memory.BATTERY_LEVEL);

    float batteryDepeleted =
        (Float)altitude + (timeValue * 0.1f) + (speedValue * 1.0f);

    float newBatteryLevel = (Float)(currentBattery - batteryDepeleted);

    if (newBatteryLevel > 0f) {
      heap.put(Memory.BATTERY_LEVEL, newBatteryLevel);
    } else {
      heap.put(Memory.BATTERY_LEVEL, 0f);
    }
    System.out.println("DISTANCE_TRAVELED :" + distance);
    System.out.println("BATTERY_LEVEL :" + heap.get(Memory.BATTERY_LEVEL));
  }
}
