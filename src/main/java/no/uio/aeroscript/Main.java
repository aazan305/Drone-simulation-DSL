package no.uio.aeroscript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import no.uio.aeroscript.antlr.AeroScriptLexer;
import no.uio.aeroscript.antlr.AeroScriptParser;
import no.uio.aeroscript.ast.stmt.Execution;
import no.uio.aeroscript.ast.stmt.Statement;
import no.uio.aeroscript.runtime.Interpreter;
import no.uio.aeroscript.runtime.REPL;
import no.uio.aeroscript.runtime.TypeCheck;
import no.uio.aeroscript.type.Memory;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Main {
  public static void main(String[] args) {
    try {
      // Read the AeroScript program
      //
      if (args.length == 0) {

        System.out.println("Missing file input");
        return;
      }
      String filePath = args[0];
      CharStream input = CharStreams.fromFileName(filePath);

      // Create lexer and parser
      AeroScriptLexer lexer = new AeroScriptLexer(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      AeroScriptParser parser = new AeroScriptParser(tokens);

      // Parse the program
      AeroScriptParser.ProgramContext tree = parser.program();

      // Initialize heap and stack
      HashMap<Memory, Object> heap = new HashMap<>();
      Stack<Statement> stack = new Stack<>();

      // Create interpreter
      Interpreter interpreter = new Interpreter(heap, stack);
      TypeCheck typechecker = new TypeCheck();
      ArrayList<Execution> executions = new ArrayList<Execution>();

      typechecker.visitProgram(tree);
      Object result = typechecker.visitProgram(tree);
      System.out.println("Typechecker is like -------> " + result);
      if (result == null) {
        executions = interpreter.visitProgram(tree);
        System.out.println("> AeroScript Program Loaded");
        System.out.println("> Found " + executions.size() + " executions");

        float initialX = (Float)heap.get(Memory.POSITION_X);
        float initialY = (Float)heap.get(Memory.POSITION_Y);
        float initialBattery = (Float)heap.get(Memory.BATTERY_LEVEL);

        System.out.println("> Initial Position: (" + initialX + ", " +
                           initialY + ")");
        System.out.println("> Initial Battery: " + initialBattery + "%");
        System.out.println("> Starting execution...\n");

        // Execute the program
        System.out.println("Starting visit Main");
        interpreter.ExcuteProgram(executions);

        System.out.println("\n> Program execution completed");
        System.out.println("> Entering REPL mode - waiting for messages...");
        System.out.println("> Type 'help' for available commands\n");

        Execution initialExecution = null;
        for (Execution exec : executions) {
          if (exec.isStart()) {
            initialExecution = exec;
            break;
          }
        }

        // Create REPL
        REPL repl =
            new REPL(heap, interpreter.getListeners(), initialExecution);
        Scanner scanner = new Scanner(System.in);
        while (!repl.isTerminating()) {
          System.out.print("MO-in> ");
          String input_line = scanner.nextLine().trim();

          if (input_line.isEmpty()) {
            continue;
          }

          String[] parts = input_line.split("\\s+", 2);
          String command = parts[0];
          String param = parts.length > 1 ? parts[1] : "";

          repl.command(command, param);
          System.out.println("\n> Final Statistics:");
          System.out.println(">   Initial Position: (" + initialX + ", " +
                             initialY + ")");
          System.out.println(">   Initial Battery: " + initialBattery + "%");
          System.out.println(">   Final Position: (" +
                             heap.get(Memory.POSITION_X) + ", " +
                             heap.get(Memory.POSITION_Y) + ")");
          System.out.println(">   Final Altitude: " +
                             heap.get(Memory.ALTITUDE));
          System.out.println(
              ">   Final Battery: " + heap.get(Memory.BATTERY_LEVEL) + "%");
          System.out.println(">   Distance Traveled: " +
                             heap.get(Memory.DISTANCE_TRAVELED));

          scanner.close();
        }

      } else {
        throw new Error("Error while checking program, couldn't visit program");
      }
    } catch (IOException e) {
      System.err.println("Error reading program file: " + e.getMessage());
      e.printStackTrace();
    } catch (Exception e) {
      System.err.println("Error during execution: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
