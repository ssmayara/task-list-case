package com.ortecfinance.tasklist.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.ortecfinance.tasklist.controller.dto.CreateProjectRequest;
import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.exceptions.NotFoundException;
import com.ortecfinance.tasklist.mapper.ProjectMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.repository.ProjectRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock
  private ProjectRepository projectRepository;

  @Mock
  private ProjectMapper projectMapper;

  private ProjectService projectService;

  @BeforeEach
  void setUp() {
    projectService = new ProjectService(projectRepository, projectMapper);
  }

  @Test
  void it_creates_project_and_calls_repository_save() {
    CreateProjectRequest request = mock(CreateProjectRequest.class);
    when(request.name()).thenReturn("My Project");

    Project saved = mock(Project.class);
    when(projectRepository.save(any(Project.class))).thenReturn(saved);

    ProjectRecord record = mock(ProjectRecord.class);
    when(projectMapper.toRecord(saved)).thenReturn(record);

    ProjectRecord result = projectService.createProject(request);

    assertSame(record, result);
    verify(projectRepository, times(1)).save(any(Project.class));
    verify(projectMapper, times(1)).toRecord(saved);
    verifyNoMoreInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_request_is_null_and_does_not_call_db() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(null));

    assertEquals("Project name must not be blank", ex.getMessage());
    verifyNoInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_request_name_is_null_and_does_not_call_db() {
    CreateProjectRequest request = mock(CreateProjectRequest.class);
    when(request.name()).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(request));

    assertEquals("Project name must not be blank", ex.getMessage());
    verifyNoInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_request_name_is_blank_and_does_not_call_db() {
    CreateProjectRequest request = mock(CreateProjectRequest.class);
    when(request.name()).thenReturn("   ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(request));

    assertEquals("Project name must not be blank", ex.getMessage());
    verifyNoInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_gets_all_projects_and_calls_repository_find_all() {
    List<Project> projects = List.of(mock(Project.class), mock(Project.class));
    when(projectRepository.findAll()).thenReturn(projects);

    List<ProjectRecord> records = List.of(mock(ProjectRecord.class));
    when(projectMapper.toRecords(projects)).thenReturn(records);

    List<ProjectRecord> result = projectService.getAllProjects();

    assertSame(records, result);
    verify(projectRepository, times(1)).findAll();
    verify(projectMapper, times(1)).toRecords(projects);
    verifyNoMoreInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_finds_by_id_and_calls_repository_find_by_id() {
    Project project = mock(Project.class);
    when(projectRepository.findById(10)).thenReturn(Optional.of(project));

    Project result = projectService.findById(10);

    assertSame(project, result);
    verify(projectRepository, times(1)).findById(10);
    verifyNoMoreInteractions(projectRepository);
    verifyNoInteractions(projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_id_not_found_and_calls_repository_find_by_id() {
    when(projectRepository.findById(99)).thenReturn(Optional.empty());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.findById(99));

    assertEquals("Project not found 99", ex.getMessage());
    verify(projectRepository, times(1)).findById(99);
    verifyNoMoreInteractions(projectRepository);
    verifyNoInteractions(projectMapper);
  }


  @Test
  void it_finds_by_name_and_calls_repository_find_by_name() {
    Project project = mock(Project.class);
    when(projectRepository.findByName("MyName")).thenReturn(Optional.of(project));

    Project result = projectService.findByName("  MyName  ");

    assertSame(project, result);
    verify(projectRepository, times(1)).findByName("MyName");
    verifyNoMoreInteractions(projectRepository);
    verifyNoInteractions(projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_name_is_null_and_does_not_call_db() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.findByName(null));

    assertEquals("Project name must not be blank", ex.getMessage());
    verifyNoInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_throws_illegal_argument_when_name_is_blank_and_does_not_call_db() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> projectService.findByName("   "));

    assertEquals("Project name must not be blank", ex.getMessage());
    verifyNoInteractions(projectRepository, projectMapper);
  }

  @Test
  void it_throws_not_found_when_name_not_found_and_calls_repository_find_by_name() {
    when(projectRepository.findByName("Nope")).thenReturn(Optional.empty());

    NotFoundException ex =
        assertThrows(NotFoundException.class, () -> projectService.findByName("Nope"));

    assertEquals("Could not find a project with the name \"Nope\".", ex.getMessage());
    verify(projectRepository, times(1)).findByName("Nope");
    verifyNoMoreInteractions(projectRepository);
    verifyNoInteractions(projectMapper);
  }
}
