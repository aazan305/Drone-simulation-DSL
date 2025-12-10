package no.uio.aeroscript.runtime;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.type.Memory;

class Command {
  private final String name;
  private final REPL repl;
  private final Function<String, Boolean> command;
  private final String help;
  private final boolean requiresParameter;
  private final String parameterHelp;

  public Command(String name, REPL repl, Function<String, Boolean> command,
                 String help, boolean requiresParameter, String parameterHelp) {
    this.name = name;
    this.repl = repl;
    this.command = command;
    this.help = help;
    this.requiresParameter = requiresParameter;
    this.parameterHelp = parameterHelp;
  }

  public String getName() { return name; }

  public String getHelp() { return help; }

  public boolean isRequiresParameter() { return requiresParameter; }

  public String getParameterHelp() { return parameterHelp; }

  public boolean execute(String param) {
    if (requiresParameter && Objects.equals(param, "")) {
      System.out.println(name + " requires a parameter: " + parameterHelp);
      return false;
    }

    return command.apply(param);
  }
}

public class REPL {
  private final HashMap<Memory, Object> heap;
  private final HashMap<String, Command> commands = new HashMap<>();
  private final Map<String, Runnable> listeners;
  private final Execution initialExecution;
  private boolean terminating = false;

  public REPL(HashMap<Memory, Object> heap, Map<String, Runnable> listeners,
              Execution initialExecution) {
    this.heap = heap;
    this.listeners = listeners;
    this.initialExecution = initialExecution;
    initCommands();
  }

  public boolean isTerminating() { return terminating; }

  private void printRepl(String str) { System.out.println("MO-out> " + str); }

  public boolean command(String str, String param) {
    LocalTime start = LocalTime.now();
    boolean result;

    if (str.equals("help")) {
      printRepl("Available commands:");
      for (Command cmd : commands.values()) {
        System.out.printf("%11s - %s%n\n", cmd.getName(), cmd.getHelp());
        if (!Objects.equals(cmd.getParameterHelp(), "")) {
          System.out.printf("%14s\n", cmd.getParameterHelp());
        }
      }
      result = false;
    } else {
      Command cmd = commands.get(str);

      if (cmd == null) {
        printRepl("Unknown command: " + str +
                  ". Type \"help\" to get a list of available commands");
        result = false;
      } else {
        try {
          cmd.execute(param);
          result = true;
        } catch (Exception e) {
          printRepl(
              "Command " + str + " " + param +
              " caused an exception. Internal state may be inconsistent");
          result = false;
        }
      }
    }

    float time = Duration.between(start, LocalTime.now()).toMillis() / 1000f;
    printRepl("Command executed in " + time + " seconds");

    return result;
  }

  private void initCommands() {
    commands.put("help", new Command("help", this, (param) -> {
                   return false;
                 }, "Show available commands", false, ""));

    commands.put(
        "message",
        new Command("message", this,
                    (param)
                        -> {
                      Runnable listener = listeners.get(param);
                      if (listener != null) {
                        printRepl("Sending message: " + param);
                        listener.run();
                        return true;
                      } else {
                        printRepl("No listener registered for message: " +
                                  param);
                        printRepl("Available messages: " + listeners.keySet());
                        return false;
                      }
                    },
                    "Send a message to trigger an event", true,
                    "The message ID to send"));

    commands.put(
        "info", new Command("info", this, (param) -> {
          printRepl("Current System State:");
          printRepl("  Position: (" + heap.get(Memory.POSITION_X) + ", " +
                    heap.get(Memory.POSITION_Y) + ")");
          printRepl("  Altitude: " + heap.get(Memory.ALTITUDE));
          printRepl("  Battery Level: " + heap.get(Memory.BATTERY_LEVEL) + "%");
          printRepl("  Distance Traveled: " +
                    heap.get(Memory.DISTANCE_TRAVELED));
          printRepl("  Initial Mode: " + heap.get(Memory.INITIAL_MODE));
          return true;
        }, "Display current system state", false, ""));

    commands.put(
        "battery", new Command("battery", this, (param) -> {
          printRepl("Battery Level: " + heap.get(Memory.BATTERY_LEVEL) + "%");
          return true;
        }, "Display current battery level", false, ""));

    commands.put("position", new Command("position", this, (param) -> {
                   printRepl("Current Position: (" +
                             heap.get(Memory.POSITION_X) + ", " +
                             heap.get(Memory.POSITION_Y) + ")");
                   printRepl("Current Altitude: " + heap.get(Memory.ALTITUDE));
                   return true;
                 }, "Display current position and altitude", false, ""));

    commands.put("messages", new Command("messages", this, (param) -> {
                   printRepl("Registered message listeners:");
                   if (listeners.isEmpty()) {
                     printRepl("  None");
                   } else {
                     for (String msg : listeners.keySet()) {
                       printRepl("  - " + msg);
                     }
                   }
                   return true;
                 }, "List all registered message listeners", false, ""));

    commands.put("exit", new Command("exit", this, (param) -> {
                   terminate();
                   return true;
                 }, "Exit the REPL and terminate the program", false, ""));
  }

  public void terminate() {
    printRepl("Terminating the REPL");
    this.terminating = true;
  }
}
