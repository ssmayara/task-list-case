package com.ortecfinance.tasklist.controller;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.service.ProjectService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/projects")
public class ProjectController {

  private final ProjectService projectService;

  public ProjectController(ProjectService projectService) {
    this.projectService = projectService;
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
    TaskRecord created = projectService.createTaskForProject(projectId, request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

}
