package no.uio.aeroscript.ast.stmt;

import java.util.HashMap;
import no.uio.aeroscript.type.Memory;

public abstract class Statement {
  public abstract void execute(HashMap<Memory, Object> heap);
}
