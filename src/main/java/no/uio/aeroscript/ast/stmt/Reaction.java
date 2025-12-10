package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.type.Memory;

public class Reaction extends Statement {
  private String eventType;
  private String targetExecution;
  private String messageId;

  public Reaction(String eventType, String targetExecution, String messageId) {
    this.eventType = eventType;
    this.messageId = messageId;
    this.targetExecution = targetExecution;
  }

  public String getEventType() { return eventType; }
  public String getMessageId() { return messageId; }
  public String getTargetExecution() { return targetExecution; }
  @Override
  public void execute(HashMap<Memory, Object> heap) {}
}
