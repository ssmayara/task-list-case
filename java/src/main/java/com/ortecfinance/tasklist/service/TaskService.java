package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.exceptions.NotFoundException;
import com.ortecfinance.tasklist.mapper.TaskMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;
  private final ProjectService projectService;

  public TaskService(TaskRepository taskRepository, TaskMapper taskMapper,
      ProjectService projectService) {
    this.taskRepository = taskRepository;
    this.taskMapper = taskMapper;
    this.projectService = projectService;
  }

  public List<TaskRecord> getAllTasks() {
    return taskMapper.toRecords(taskRepository.findAll());
  }

  @Transactional
  public TaskRecord createTaskForProject(Integer projectId, CreateTaskRequest request) {
    if (request == null || request.description() == null || request.description().isBlank()) {
      throw new IllegalArgumentException("Task description must not be blank");
    }

    Project project = projectService.findById(projectId);

    Task task = new Task();
    task.setDescription(request.description().trim());
    task.setStatus(request.status().name());
    task.setDeadline(request.deadline());
    task.setProject(project);

    Task saved = taskRepository.save(task);
    return taskMapper.toRecord(saved);
  }

  @Transactional
  public TaskRecord updateDeadline(Integer projectId, Integer taskId, LocalDate deadline) {
    Task task = taskRepository.findByIdAndProjectId(taskId, projectId)
        .orElseThrow(() -> new NotFoundException(
            "Task " + taskId + " not found for project " + projectId));

    task.setDeadline(deadline);
    return taskMapper.toRecord(task);
  }

}
