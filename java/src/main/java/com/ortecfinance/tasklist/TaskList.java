package com.ortecfinance.tasklist;

import static com.ortecfinance.tasklist.Utils.Util.normalize;
import static com.ortecfinance.tasklist.Utils.Util.parseIntOrNull;
import static com.ortecfinance.tasklist.Utils.Util.split2;
import static com.ortecfinance.tasklist.Utils.Util.splitArgsRespectingQuotes;
import static com.ortecfinance.tasklist.Utils.Util.stripOuterQuotes;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.service.ProjectService;
import com.ortecfinance.tasklist.service.TaskDeadlineViewService;
import com.ortecfinance.tasklist.service.TaskService;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public final class TaskList implements Runnable {

    private static final String QUIT = "quit";
    private static final DateTimeFormatter DEADLINE_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final BufferedReader in;
    private final PrintWriter out;

    private final ProjectService projectService;
    private final TaskService taskService;
    private final TaskDeadlineViewService viewService;

    public TaskList(
        BufferedReader in,
        PrintWriter out,
        ProjectService projectService,
        TaskService taskService,
        TaskDeadlineViewService viewService
    ) {
        this.in = in;
        this.out = out;
        this.projectService = projectService;
        this.taskService = taskService;
        this.viewService = viewService;
    }

    @Override
    public void run() {
        ConsolePrinter.printWelcome(out);

        while (true) {
            ConsolePrinter.printPrompt(out);

            String line;
            try {
                line = in.readLine();
            } catch (IOException e) {
                if (Thread.currentThread().isInterrupted() || e instanceof java.io.InterruptedIOException) {
                    return;
                }
                throw new RuntimeException(e);
            }

            if (line == null) {
                break;
            }

            String input = normalize(line);
            if (input.isEmpty()) {
                continue;
            }

            if (QUIT.equalsIgnoreCase(input)) {
                break;
            }

            execute(input);
        }
    }

    private void execute(String input) {
        String[] top = split2(input);
        String cmd = top[0].toLowerCase();
        String rest = top.length == 2 ? top[1] : "";

        switch (cmd) {
            case "show" -> handleShow();
            case "add" -> handleAdd(rest);
            case "check" -> handleSetDone(rest, true);
            case "uncheck" -> handleSetDone(rest, false);
            case "deadline" -> handleDeadline(rest);
            case "view-by-deadline" -> handleViewByDeadline();
            case "help" -> ConsolePrinter.printHelp(out);
            default -> ConsolePrinter.printUnknownCommand(out, cmd);
        }
    }

    private void handleShow() {
        try {
            List<ProjectRecord> projects = projectService.getAllProjects();
            ConsolePrinter.printShow(out, projects);
        } catch (Exception ex) {
            out.println(ex.getMessage());
        }
    }

    private void handleAdd(String rest) {
        String[] sub = split2(normalize(rest));
        String type = sub[0].toLowerCase();
        String args = sub.length == 2 ? sub[1].trim() : "";

        switch (type) {
            case "project" -> handleAddProject(args);
            case "task" -> handleAddTask(args);
            default -> ConsolePrinter.printUnknownCommand(out, "add " + type);
        }
    }

    private void handleAddProject(String name) {
        name = stripOuterQuotes(normalize(name));
        if (name.isBlank()) return;

        projectService.createProject(new CreateProjectRequest(name));
    }

    private void handleAddTask(String args) {
        List<String> tokens = splitArgsRespectingQuotes(normalize(args));
        if (tokens.size() < 2) return;

        String projectName = stripOuterQuotes(tokens.get(0));
        String description = stripOuterQuotes(String.join(" ", tokens.subList(1, tokens.size())));

        Project project = projectService.findByName(projectName);
        if (project == null) {
            ConsolePrinter.printProjectNotFound(out, projectName);
            return;
        }

        taskService.createTaskForProject(
            project.getId(),
            new CreateTaskRequest(description, false, null)
        );
    }

    private void handleSetDone(String rest, boolean done) {
        Integer taskId = parseIntOrNull(normalize(rest));
        if (taskId == null) {
            return;
        }

        try {
            taskService.updateCompleted(taskId, done);
        } catch (Exception ex) {
            ConsolePrinter.printTaskNotFound(out, taskId);
        }
    }

    private void handleDeadline(String rest) {
        String[] parts = split2(normalize(rest));
        if (parts.length < 2) {
            ConsolePrinter.printUsage(out, "deadline <task ID> <dd-MM-yyyy>");
            return;
        }

        Integer taskId = parseIntOrNull(parts[0]);
        if (taskId == null) {
            ConsolePrinter.printError(out, "Invalid task ID: " + parts[0]);
            return;
        }

        LocalDate deadline;
        try {
            deadline = LocalDate.parse(parts[1], DEADLINE_FMT);
        } catch (DateTimeParseException e) {
            ConsolePrinter.printError(out, "Invalid date. Use dd-MM-yyyy");
            return;
        }

        try {
            taskService.updateDeadline(taskId, deadline);
        } catch (Exception ex) {
            ConsolePrinter.printTaskNotFound(out, taskId);
        }
    }

    private void handleViewByDeadline() {
        try {
            String output = viewService.viewByDeadlineGroupedByProject();
            if (output != null && !output.isEmpty()) {
                ConsolePrinter.printRaw(out, output);
            }
        } catch (Exception ex) {
            ConsolePrinter.printError(out, ex.getMessage());
        }
    }
}
