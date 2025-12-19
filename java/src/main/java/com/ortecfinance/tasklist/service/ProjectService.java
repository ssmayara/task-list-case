package com.ortecfinance.tasklist.service;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.exceptions.NotFoundException;
import com.ortecfinance.tasklist.mapper.ProjectMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.repository.ProjectRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;

  public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
    this.projectRepository = projectRepository;
    this.projectMapper = projectMapper;
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
    Project project = projectRepository.findById(projectId)
        .orElseThrow(() ->
            new IllegalArgumentException("Project not found " + projectId)
        );

    return project;
  }

  @Transactional(readOnly = true)
  public Project findByName(String name) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("Project name must not be blank");
    }

    return projectRepository.findByName(name.trim())
        .orElseThrow(() ->
            new NotFoundException("Could not find a project with the name \"" + name + "\".")
        );
  }

}
