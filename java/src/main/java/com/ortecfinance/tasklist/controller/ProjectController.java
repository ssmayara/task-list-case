package com.ortecfinance.tasklist.controller;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.controller.dto.TaskResponse;
import com.ortecfinance.tasklist.service.ProjectService;
import com.ortecfinance.tasklist.service.TaskDeadlineViewService;
import com.ortecfinance.tasklist.service.TaskService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;
  private final TaskDeadlineViewService taskDeadlineViewService;
  private final TaskService taskService;

  public ProjectController(ProjectService projectService,
      TaskDeadlineViewService taskDeadlineViewService, TaskService taskService) {
    this.projectService = projectService;
    this.taskDeadlineViewService = taskDeadlineViewService;
    this.taskService = taskService;
  }

  @PostMapping
  public ProjectRecord create(@RequestBody CreateProjectRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request))
        .getBody();
  }

  @GetMapping
  public ResponseEntity<List<ProjectRecord>> getAllProducts() {
    return ResponseEntity.ok(projectService.getAllProjects());
  }

  @PostMapping("/{projectId}/tasks")
  public ResponseEntity<TaskRecord> createTask(@PathVariable Integer projectId,
      @RequestBody CreateTaskRequest request) {
    TaskRecord created = taskService.createTaskForProject(projectId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PutMapping("/{projectId}/tasks/{taskId}")
  public ResponseEntity<TaskRecord> updateDeadline(
      @PathVariable Integer projectId,
      @PathVariable Integer taskId,
      @RequestParam(required = false)
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
      LocalDate deadline
  ) {
    TaskRecord updated = taskService.updateDeadline(projectId, taskId, deadline);
    return ResponseEntity.ok(updated);
  }

//  @GetMapping(value = "/view_by_deadline", produces = MediaType.TEXT_PLAIN_VALUE)
//  public ResponseEntity<String> viewByDeadline() {
//    return ResponseEntity.ok(taskDeadlineViewService.viewByDeadlineGroupedByProject());
//  }

  @GetMapping(value = "/view_by_deadline", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Map<String, List<TaskResponse>>>> viewByDeadline() {
    return ResponseEntity.ok(taskDeadlineViewService.viewByDeadlineGroupedByProjectJson());
  }

}
