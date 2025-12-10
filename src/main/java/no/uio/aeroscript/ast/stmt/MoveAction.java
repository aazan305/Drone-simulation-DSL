package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.ast.expr.*;
import no.uio.aeroscript.type.Memory;
import no.uio.aeroscript.type.Point;

public class MoveAction extends Action {
  private Expression time;
  private Expression speed;
  private Point point;
  private Expression distance;
  private boolean isPoint;

  public MoveAction(Expression time, Expression speed, Point point) {
    this.time = time;
    this.speed = speed;
    this.point = point;
    this.isPoint = true;
  }

  public MoveAction(Expression time, Expression speed, Expression distance) {
    this.time = time;
    this.speed = speed;
    this.distance = distance;
    this.isPoint = false;
  }
  @Override
  public void execute(HashMap<Memory, Object> heap) {
    float positionx = (Float)heap.get(Memory.POSITION_X);
    float positiony = (Float)heap.get(Memory.POSITION_Y);
    float batterylevel = (Float)heap.get(Memory.BATTERY_LEVEL);
    float oldDistance = (Float)heap.get(Memory.DISTANCE_TRAVELED);
    float timeExp = (Float)time.evaluate();
    float speedExp = (Float)speed.evaluate();
    if (isPoint) {

      float targetx = (Float)point.getX();
      float targety = (Float)point.getY();
      System.out.println("Moving to point (" + targetx + " , " + targety + ")"
                         + "at SPEED :" + speedExp + "for " + timeExp +
                         " seconds.");
      heap.put(Memory.POSITION_X, targetx);
      heap.put(Memory.POSITION_Y, targety);
      float deltax = targetx - positionx;
      float deltay = targety - positiony;
      float objectiveDistance =
          (float)Math.sqrt((deltax * deltax) + (deltay * deltay));
      heap.put(Memory.DISTANCE_TRAVELED, (objectiveDistance + oldDistance));
      float batteryDepleted =
          ((objectiveDistance * 0.7f) * (timeExp * 0.1f) + (speedExp * 1f));
      if (batterylevel - batteryDepleted > 0f) {

        heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepleted);
      } else {
        heap.put(Memory.BATTERY_LEVEL, 0f);
      }
      // add distance with sqrt -- to find
      // distacnetraveled or just one of x or y if
      // only 1 is given, and put in heap, check Dock
      // for errors;
    } else {

      float targetDistance = (Float)distance.evaluate();
      System.out.println("Moving by :" + targetDistance + "at SPEED :" +
                         speedExp + "for " + timeExp + " seconds.");
      heap.put(Memory.POSITION_X, (positionx + targetDistance));
      heap.put(Memory.DISTANCE_TRAVELED, (targetDistance + oldDistance));

      float batteryDepleted =
          ((targetDistance * 0.5f) + (timeExp * 0.1f) + (speedExp * 1f));
      if (batterylevel - batteryDepleted > 0f) {

        heap.put(Memory.BATTERY_LEVEL, batterylevel - batteryDepleted);
      } else {
        heap.put(Memory.BATTERY_LEVEL, 0f);
      }
    }
  }
}
