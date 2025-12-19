package com.ortecfinance.tasklist;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.service.ProjectService;
import com.ortecfinance.tasklist.service.TaskDeadlineViewService;
import com.ortecfinance.tasklist.service.TaskService;
import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public final class ApplicationTest {

  public static final String PROMPT = "> ";

  private final PipedOutputStream inStream = new PipedOutputStream();
  private final PrintWriter inWriter = new PrintWriter(inStream, true);

  private final PipedInputStream outStream = new PipedInputStream();
  private final BufferedReader outReader = new BufferedReader(new InputStreamReader(outStream));

  private Thread applicationThread;

  private ProjectService projectService;
  private TaskService taskService;
  private TaskDeadlineViewService viewService;

  private final Map<String, Project> projectsByName = new LinkedHashMap<>();
  private final Map<Integer, String> projectNameById = new HashMap<>();
  private final Map<String, List<TaskRecord>> tasksByProjectName = new LinkedHashMap<>();
  private final AtomicInteger projectIdSeq = new AtomicInteger(0);
  private final AtomicInteger taskIdSeq = new AtomicInteger(0);

  public ApplicationTest() throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(new PipedInputStream(inStream)));
    PrintWriter out = new PrintWriter(new PipedOutputStream(outStream), true);

    projectService = mock(ProjectService.class);
    taskService = mock(TaskService.class);
    viewService = mock(TaskDeadlineViewService.class);

    stubMocks();

    TaskList taskList = new TaskList(in, out, projectService, taskService, viewService);
    applicationThread = new Thread(taskList);
  }

  @BeforeEach
  public void start_the_application() throws IOException {
    applicationThread.start();
    readLines("Welcome to TaskList! Type 'help' for available commands.");
  }

  @AfterEach
  public void kill_the_application() throws Exception {
    if (!stillRunning()) return;

    try {
      execute("quit");
    } catch (Exception ignored) {
    }

    Thread.sleep(300);

    if (stillRunning()) {
      applicationThread.interrupt();
      Thread.sleep(300);
    }

    if (stillRunning()) {
      throw new IllegalStateException("The application is still running.");
    }
  }

  @Test
  void it_works() throws IOException {
    execute("show");

    execute("add project secrets");
    execute("add task secrets Eat more donuts.");
    execute("add task secrets Destroy all humans.");

    execute("show");
    readLines(
        "secrets",
        "    [ ] 1: Eat more donuts.",
        "    [ ] 2: Destroy all humans.",
        ""
    );

    execute("add project training");
    execute("add task training Four Elements of Simple Design");
    execute("add task training SOLID");
    execute("add task training Coupling and Cohesion");
    execute("add task training Primitive Obsession");
    execute("add task training Outside-In TDD");
    execute("add task training Interaction-Driven Design");

    execute("check 1");
    execute("check 3");
    execute("check 5");
    execute("check 6");

    execute("show");
    readLines(
        "secrets",
        "    [x] 1: Eat more donuts.",
        "    [ ] 2: Destroy all humans.",
        "",
        "training",
        "    [x] 3: Four Elements of Simple Design",
        "    [ ] 4: SOLID",
        "    [x] 5: Coupling and Cohesion",
        "    [x] 6: Primitive Obsession",
        "    [ ] 7: Outside-In TDD",
        "    [ ] 8: Interaction-Driven Design",
        ""
    );

    execute("quit");
  }

  @Test
  void view_by_deadline_prints_output_when_non_empty() throws IOException {
    when(viewService.viewByDeadlineGroupedByProject()).thenReturn(
        "secrets" + lineSeparator() +
            "    01-01-2026: Eat more donuts." + lineSeparator() +
            lineSeparator()
    );

    execute("view-by-deadline");

    readLines(
        "secrets",
        "    01-01-2026: Eat more donuts.",
        ""
    );

    verify(viewService).viewByDeadlineGroupedByProject();
  }

  @Test
  void view_by_deadline_prints_error_when_service_throws() throws IOException {
    when(viewService.viewByDeadlineGroupedByProject()).thenThrow(new RuntimeException("boom"));

    execute("view-by-deadline");

    readLines(
        "boom",
        ""
    );

    verify(viewService).viewByDeadlineGroupedByProject();
  }

  @Test
  void deadline_prints_usage_when_missing_args() throws IOException {
    execute("deadline 1");

    readLines(
        "Usage: deadline <task ID> <dd-MM-yyyy>",
        ""
    );

    verifyNoInteractions(taskService);
  }

  @Test
  void deadline_prints_error_when_task_id_invalid() throws IOException {
    execute("deadline abc 01-01-2026");

    readLines(
        "Invalid task ID: abc",
        ""
    );

    verifyNoInteractions(taskService);
  }

  @Test
  void deadline_prints_error_when_date_invalid() throws IOException {
    execute("deadline 1 2026-01-01");

    readLines(
        "Invalid date. Use dd-MM-yyyy",
        ""
    );

    verifyNoInteractions(taskService);
  }

  private void execute(String command) throws IOException {
    read(PROMPT);
    write(command);
  }

  private void read(String expectedOutput) throws IOException {
    int length = expectedOutput.length();
    char[] buffer = new char[length];

    int off = 0;
    while (off < length) {
      int r = outReader.read(buffer, off, length - off);
      if (r == -1) throw new EOFException("stdout fechado");
      off += r;
    }

    assertThat(String.valueOf(buffer), is(expectedOutput));
  }

  private void readLines(String... expectedOutput) throws IOException {
    for (String line : expectedOutput) {
      read(line + lineSeparator());
    }
  }

  private void write(String input) {
    inWriter.println(input);
  }

  private boolean stillRunning() {
    return applicationThread != null && applicationThread.isAlive();
  }

  private void stubMocks() {

    doAnswer(inv -> {
      CreateProjectRequest req = inv.getArgument(0);
      String name = req.name();

      int id = projectIdSeq.incrementAndGet();

      Project p = new Project();
      p.setId(id);
      p.setName(name);

      projectsByName.put(name, p);
      projectNameById.put(id, name);
      tasksByProjectName.putIfAbsent(name, new ArrayList<>());

      return null;
    }).when(projectService).createProject(any(CreateProjectRequest.class));

    when(projectService.findByName(anyString())).thenAnswer(inv -> {
      String name = inv.getArgument(0);
      return projectsByName.get(name);
    });

    when(projectService.getAllProjects()).thenAnswer(inv -> {
      List<ProjectRecord> list = new ArrayList<>();
      for (Map.Entry<String, List<TaskRecord>> entry : tasksByProjectName.entrySet()) {
        String name = entry.getKey();
        List<TaskRecord> tasks = entry.getValue();

        Project project = projectsByName.get(name);
        Integer projectId = project != null ? project.getId() : null;

        list.add(new ProjectRecord(projectId, name, tasks));
      }
      return list;
    });

    when(taskService.createTaskForProject(anyInt(), any(CreateTaskRequest.class))).thenAnswer(inv -> {
      int projectId = inv.getArgument(0);
      CreateTaskRequest req = inv.getArgument(1);

      String projectName = projectNameById.get(projectId);
      if (projectName == null) throw new IllegalArgumentException("Project not found: " + projectId);

      int taskId = taskIdSeq.incrementAndGet();
      TaskRecord created = new TaskRecord(taskId, req.description(), req.completed(), req.deadline());

      tasksByProjectName.computeIfAbsent(projectName, k -> new ArrayList<>()).add(created);
      return created;
    });

    when(taskService.updateCompleted(anyInt(), anyBoolean())).thenAnswer(inv -> {
      int taskId = inv.getArgument(0);
      boolean completed = inv.getArgument(1);

      for (List<TaskRecord> list : tasksByProjectName.values()) {
        for (int i = 0; i < list.size(); i++) {
          TaskRecord t = list.get(i);
          if (t.id().equals(taskId)) {
            TaskRecord updated = new TaskRecord(t.id(), t.description(), completed, t.deadline());
            list.set(i, updated);
            return updated;
          }
        }
      }
      throw new IllegalArgumentException("Task not found: " + taskId);
    });

    when(taskService.updateDeadline(anyInt(), any(LocalDate.class))).thenAnswer(inv -> {
      int taskId = inv.getArgument(0);
      LocalDate deadline = inv.getArgument(1);

      for (List<TaskRecord> list : tasksByProjectName.values()) {
        for (int i = 0; i < list.size(); i++) {
          TaskRecord t = list.get(i);
          if (t.id().equals(taskId)) {
            TaskRecord updated = new TaskRecord(t.id(), t.description(), t.completed(), deadline);
            list.set(i, updated);
            return updated;
          }
        }
      }
      throw new IllegalArgumentException("Task not found: " + taskId);
    });

    when(viewService.viewByDeadlineGroupedByProject()).thenReturn("");
  }
}
