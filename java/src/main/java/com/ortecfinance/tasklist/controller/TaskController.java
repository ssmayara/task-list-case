package com.ortecfinance.tasklist.controller;

import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.service.TaskService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tasks")
public class TaskController {

  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

//    @GetMapping
//    public List<String> getTasks() {
//        return Arrays.asList("Task 1", "Task 2", "Task 3");
//    }

  @GetMapping
  public ResponseEntity<List<TaskRecord>> getAllTasks() {
    return ResponseEntity.ok(taskService.getAllTasks());
  }
}
