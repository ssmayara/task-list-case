package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.mapper.TaskMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaskService {

  private final TaskRepository taskRepository;
  private final TaskMapper taskMapper;

  public TaskService(TaskRepository taskRepository, TaskMapper taskMapper) {
    this.taskRepository = taskRepository;
    this.taskMapper = taskMapper;
  }

  public List<TaskRecord> getAllTasks() {
    return taskMapper.toRecords(taskRepository.findAll());
  }

  @Transactional
  public TaskRecord createTask(Project project, CreateTaskRequest request) {
    if (request == null || request.description() == null || request.description().isBlank()) {
      throw new IllegalArgumentException("Task description must not be blank");
    }

    Task task = new Task();
    task.setDescription(request.description().trim());
    task.setStatus(request.status().name());
    task.setDeadline(request.deadline());
    task.setProject(project);

    Task saved = taskRepository.save(task);
    return taskMapper.toRecord(saved);
  }

}
