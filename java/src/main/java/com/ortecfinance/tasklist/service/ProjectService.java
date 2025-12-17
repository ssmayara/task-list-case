package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.mapper.ProjectMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.repository.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;

  private final TaskService taskService;

  public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper,
      TaskService taskService) {
    this.projectRepository = projectRepository;
    this.projectMapper = projectMapper;
    this.taskService = taskService;
  }

  @Transactional
  public ProjectRecord createProject(CreateProjectRequest request) {
    if (request == null || request.name() == null || request.name().isBlank()) {
      throw new IllegalArgumentException("Project name must not be blank");
    }

    Project project = new Project(request.name());
    Project saved = projectRepository.save(project);

    return projectMapper.toRecord(saved);
  }

  @Transactional(readOnly = true)
  public List<ProjectRecord> getAllProjects() {
    return projectMapper.toRecords(projectRepository.findAll());
  }

  @Transactional(readOnly = true)
  public Project findById(Integer projectId) {
    Project project = projectRepository.findById(Long.valueOf(projectId))
        .orElseThrow(() ->
            new IllegalArgumentException("Project not found: " + projectId)
        );

    return project;
  }

  public TaskRecord createTaskForProject(Integer projectId, CreateTaskRequest request) {
    Optional<Project> project = projectRepository.findById(Long.valueOf(projectId));
    return taskService.createTask(project.get(), request);
  }
}
