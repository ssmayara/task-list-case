package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.controller.dto.TaskResponse;
import com.ortecfinance.tasklist.mapper.TaskMapper;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.repository.TaskRepository;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskDeadlineViewService {

  private static final DateTimeFormatter OUT_FMT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;

  public TaskDeadlineViewService(TaskRepository taskRepository, TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.taskMapper = taskMapper;
  }

  @Transactional(readOnly = true)
  public String viewByDeadlinePlainText() {
    List<Task> tasks = taskRepository.findAll();

    Map<LocalDate, List<Task>> byDeadline = new TreeMap<>();
    List<Task> noDeadline = new ArrayList<>();

    for (Task t : tasks) {
      if (t.getDeadline() == null) {
        noDeadline.add(t);
      } else {
        byDeadline.computeIfAbsent(t.getDeadline(), d -> new ArrayList<>()).add(t);
      }
    }

    for (List<Task> group : byDeadline.values()) {
      group.sort(Comparator.comparing(Task::getId));
    }
    noDeadline.sort(Comparator.comparing(Task::getId));

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<LocalDate, List<Task>> entry : byDeadline.entrySet()) {
      sb.append(OUT_FMT.format(entry.getKey())).append(":\n");
      for (Task t : entry.getValue()) {
        sb.append("       ")
            .append(t.getId()).append(": ")
            .append(t.getDescription())
            .append("\n");
      }
    }

    sb.append("No deadline:\n");
    for (Task t : noDeadline) {
      sb.append("       ")
          .append(t.getId()).append(": ")
          .append(t.getDescription())
          .append("\n");
    }

    return sb.toString();
  }

  @Transactional(readOnly = true)
  public String viewByDeadlineGroupedByProject() {

    List<Task> tasks = taskRepository.findAll();

    Map<LocalDate, Map<String, List<Task>>> byDeadline = new TreeMap<>();
    Map<String, List<Task>> noDeadline = new TreeMap<>();

    for (Task task : tasks) {
      String projectName = task.getProject() != null
          ? task.getProject().getName()
          : "No Project";

      if (task.getDeadline() == null) {
        noDeadline
            .computeIfAbsent(projectName, k -> new ArrayList<>())
            .add(task);
      } else {
        byDeadline
            .computeIfAbsent(task.getDeadline(), d -> new TreeMap<>())
            .computeIfAbsent(projectName, p -> new ArrayList<>())
            .add(task);
      }
    }

    StringBuilder sb = new StringBuilder();

    for (Map.Entry<LocalDate, Map<String, List<Task>>> dateEntry : byDeadline.entrySet()) {
      sb.append(OUT_FMT.format(dateEntry.getKey())).append(":\n");

      for (Map.Entry<String, List<Task>> projectEntry : dateEntry.getValue().entrySet()) {
        sb.append("     ")
            .append(projectEntry.getKey())
            .append(":\n");

        projectEntry.getValue().stream()
            .sorted(Comparator.comparing(Task::getId))
            .forEach(task ->
                sb.append("        \t")
                    .append(task.getId())
                    .append(": ")
                    .append(task.getDescription())
                    .append("\n")
            );
      }
    }

    sb.append("No deadline:\n");
    for (Map.Entry<String, List<Task>> projectEntry : noDeadline.entrySet()) {
      sb.append("     ")
          .append(projectEntry.getKey())
          .append(":\n");

      projectEntry.getValue().stream()
          .sorted(Comparator.comparing(Task::getId))
          .forEach(task ->
              sb.append("        \t")
                  .append(task.getId())
                  .append(": ")
                  .append(task.getDescription())
                  .append("\n")
          );
    }

    return sb.toString();
  }

  @Transactional(readOnly = true)
  public Map<String, List<TaskResponse>> viewByDeadlineJson() {

    List<Task> tasks = taskRepository.findAll();

    Map<LocalDate, List<Task>> byDeadline = new TreeMap<>();
    List<Task> noDeadline = new ArrayList<>();

    for (Task t : tasks) {
      if (t.getDeadline() == null) {
        noDeadline.add(t);
      } else {
        byDeadline
            .computeIfAbsent(t.getDeadline(), d -> new ArrayList<>())
            .add(t);
      }
    }

    Map<String, List<TaskResponse>> result = new LinkedHashMap<>();

    byDeadline.forEach((date, list) -> {
      List<TaskResponse> responses = list.stream()
          .sorted(Comparator.comparing(Task::getId))
          .map(taskMapper::toResponse)
          .toList();

      result.put(date.toString(), responses);
    });

    List<TaskResponse> noDeadlineResponses = noDeadline.stream()
        .sorted(Comparator.comparing(Task::getId))
        .map(taskMapper::toResponse)
        .toList();

    result.put("noDeadline", noDeadlineResponses);

    return result;
  }

  @Transactional(readOnly = true)
  public Map<String, Map<String, List<TaskResponse>>> viewByDeadlineGroupedByProjectJson() {

    List<Task> tasks = taskRepository.findAll();

    Map<LocalDate, Map<String, List<Task>>> byDeadline = new TreeMap<>();
    Map<String, List<Task>> noDeadline = new TreeMap<>();

    for (Task task : tasks) {
      String projectName = task.getProject() != null
          ? task.getProject().getName()
          : "No Project";

      if (task.getDeadline() == null) {
        noDeadline
            .computeIfAbsent(projectName, k -> new ArrayList<>())
            .add(task);
      } else {
        byDeadline
            .computeIfAbsent(task.getDeadline(), d -> new TreeMap<>())
            .computeIfAbsent(projectName, p -> new ArrayList<>())
            .add(task);
      }
    }

    Map<String, Map<String, List<TaskResponse>>> result = new LinkedHashMap<>();

    byDeadline.forEach((date, projects) -> {
      Map<String, List<TaskResponse>> projectResponses = new LinkedHashMap<>();

      projects.forEach((projectName, list) -> {
        List<TaskResponse> responses = list.stream()
            .sorted(Comparator.comparing(Task::getId))
            .map(taskMapper::toResponse)
            .toList();

        projectResponses.put(projectName, responses);
      });

      result.put(date.toString(), projectResponses);
    });

    Map<String, List<TaskResponse>> noDeadlineResponses = new LinkedHashMap<>();
    noDeadline.forEach((projectName, list) -> {
      List<TaskResponse> responses = list.stream()
          .sorted(Comparator.comparing(Task::getId))
          .map(taskMapper::toResponse)
          .toList();

      noDeadlineResponses.put(projectName, responses);
    });

    result.put("noDeadline", noDeadlineResponses);

    return result;
  }


}
