package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import java.io.PrintWriter;
import java.util.List;

public final class ConsolePrinter {

  private ConsolePrinter() {
  }

  public static void printWelcome(PrintWriter out) {
    out.println("Welcome to TaskList! Type 'help' for available commands.");
  }

  public static void printPrompt(PrintWriter out) {
    out.print("> ");
    out.flush();
  }

  public static void printUnknownCommand(PrintWriter out, String command) {
    out.printf("I don't know what the command \"%s\" is.%n", command);
  }

  public static void printProjectNotFound(PrintWriter out, String projectName) {
    out.printf("Could not find a project with the name \"%s\".%n", projectName);
  }

  public static void printTaskNotFound(PrintWriter out, int taskId) {
    out.printf("Could not find a task with an ID of %d.%n", taskId);
  }

  public static void printHelp(PrintWriter out) {
    out.println("Commands:");
    out.println("  show");
    out.println("  add project \"<project name>\"");
    out.println("  add task \"<project name>\" \"<task description>\"");
    out.println("  check <task ID>");
    out.println("  uncheck <task ID>");
    out.println("  deadline <task ID> <dd-MM-yyyy>");
    out.println("  view-by-deadline");
    out.println("  quit");
    out.println();
  }

  public static void printShow(PrintWriter out, List<ProjectRecord> projects) {
    for (ProjectRecord project : projects) {
      out.println(project.name());

      if (project.tasks() != null) {
        for (TaskRecord task : project.tasks()) {
          char mark = task.completed() ? 'x' : ' ';
          out.printf(
              "    [%c] %d: %s%n",
              mark,
              task.id(),
              task.description()
          );
        }
      }
      out.println();
    }
  }

  public static void printUsage(PrintWriter out, String usage) {
    out.printf("Usage: %s%n%n", usage);
  }

  public static void printError(PrintWriter out, String message) {
    out.printf("%s%n%n", message == null ? "Error" : message);
  }

  public static void printRaw(PrintWriter out, String text) {
    out.print(text);
    if (!text.endsWith("\n")) {
      out.println();
    }
  }
}
