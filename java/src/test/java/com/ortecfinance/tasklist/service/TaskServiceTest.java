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

import com.ortecfinance.tasklist.controller.dto.CreateTaskRequest;
import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.exceptions.NotFoundException;
import com.ortecfinance.tasklist.mapper.TaskMapper;
import com.ortecfinance.tasklist.model.Project;
import com.ortecfinance.tasklist.model.Task;
import com.ortecfinance.tasklist.repository.TaskRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

  @Mock
  private TaskRepository taskRepository;

  @Mock
  private TaskMapper taskMapper;

  @Mock
  private ProjectService projectService;

  private TaskService taskService;

  @BeforeEach
  void setUp() {
    taskService = new TaskService(taskRepository, taskMapper, projectService);
  }

  @Test
  void it_gets_all_tasks_and_calls_repository_find_all() {
    List<Task> tasks = List.of(mock(Task.class), mock(Task.class));
    when(taskRepository.findAll()).thenReturn(tasks);

    List<TaskRecord> records = List.of(mock(TaskRecord.class));
    when(taskMapper.toRecords(tasks)).thenReturn(records);

    List<TaskRecord> result = taskService.getAllTasks();

    assertSame(records, result);
    verify(taskRepository, times(1)).findAll();
    verify(taskMapper, times(1)).toRecords(tasks);
    verifyNoMoreInteractions(taskRepository, taskMapper);
    verifyNoInteractions(projectService);
  }

  @Test
  void it_creates_task_for_project_and_calls_project_service_and_repository_save() {
    Integer projectId = 1;

    CreateTaskRequest request = mock(CreateTaskRequest.class);
    when(request.description()).thenReturn("  Do something  ");
    when(request.completed()).thenReturn(true);
    LocalDate deadline = LocalDate.of(2025, 1, 15);
    when(request.deadline()).thenReturn(deadline);

    Project project = mock(Project.class);
    when(projectService.findById(projectId)).thenReturn(project);

    Task saved = mock(Task.class);
    when(taskRepository.save(any(Task.class))).thenReturn(saved);

    TaskRecord record = mock(TaskRecord.class);
    when(taskMapper.toRecord(saved)).thenReturn(record);

    TaskRecord result = taskService.createTaskForProject(projectId, request);

    assertSame(record, result);

    verify(projectService, times(1)).findById(projectId);
    verify(taskRepository, times(1)).save(any(Task.class));
    verify(taskMapper, times(1)).toRecord(saved);

    verifyNoMoreInteractions(taskRepository, taskMapper, projectService);
  }

  @Test
  void it_throws_illegal_argument_when_request_is_null_and_does_not_call_db() {
    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> taskService.createTaskForProject(1, null));

    assertEquals("Task description must not be blank", ex.getMessage());
    verifyNoInteractions(taskRepository, taskMapper, projectService);
  }

  @Test
  void it_throws_illegal_argument_when_description_is_null_and_does_not_call_db() {
    CreateTaskRequest request = mock(CreateTaskRequest.class);
    when(request.description()).thenReturn(null);

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> taskService.createTaskForProject(1, request));

    assertEquals("Task description must not be blank", ex.getMessage());
    verifyNoInteractions(taskRepository, taskMapper, projectService);
  }

  @Test
  void it_throws_illegal_argument_when_description_is_blank_and_does_not_call_db() {
    CreateTaskRequest request = mock(CreateTaskRequest.class);
    when(request.description()).thenReturn("   ");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> taskService.createTaskForProject(1, request));

    assertEquals("Task description must not be blank", ex.getMessage());
    verifyNoInteractions(taskRepository, taskMapper, projectService);
  }

  @Test
  void it_updates_deadline_by_project_and_task_and_calls_repository_find_by_id_and_project_id() {
    Integer projectId = 10;
    Integer taskId = 20;
    LocalDate deadline = LocalDate.of(2026, 3, 1);

    Task task = mock(Task.class);
    when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.of(task));

    TaskRecord record = mock(TaskRecord.class);
    when(taskMapper.toRecord(task)).thenReturn(record);

    TaskRecord result = taskService.updateDeadline(projectId, taskId, deadline);

    assertSame(record, result);

    verify(taskRepository, times(1)).findByIdAndProjectId(taskId, projectId);
    verify(task, times(1)).setDeadline(deadline);
    verify(taskMapper, times(1)).toRecord(task);

    verifyNoMoreInteractions(taskRepository, taskMapper, task);
    verifyNoInteractions(projectService);
  }

  @Test
  void it_throws_not_found_when_task_not_found_for_project() {
    Integer projectId = 10;
    Integer taskId = 20;
    LocalDate deadline = LocalDate.of(2026, 3, 1);

    when(taskRepository.findByIdAndProjectId(taskId, projectId)).thenReturn(Optional.empty());

    NotFoundException ex =
        assertThrows(NotFoundException.class,
            () -> taskService.updateDeadline(projectId, taskId, deadline));

    assertEquals("Task " + taskId + " not found for project " + projectId, ex.getMessage());

    verify(taskRepository, times(1)).findByIdAndProjectId(taskId, projectId);
    verifyNoMoreInteractions(taskRepository);
    verifyNoInteractions(taskMapper, projectService);
  }

  @Test
  void it_updates_deadline_by_task_id_and_calls_repository_find_by_id() {
    Integer taskId = 7;
    LocalDate deadline = LocalDate.of(2025, 12, 31);

    Task task = mock(Task.class);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    TaskRecord record = mock(TaskRecord.class);
    when(taskMapper.toRecord(task)).thenReturn(record);

    TaskRecord result = taskService.updateDeadline(taskId, deadline);

    assertSame(record, result);

    verify(taskRepository, times(1)).findById(taskId);
    verify(task, times(1)).setDeadline(deadline);
    verify(taskMapper, times(1)).toRecord(task);

    verifyNoMoreInteractions(taskRepository, taskMapper, task);
    verifyNoInteractions(projectService);
  }

  @Test
  void it_throws_not_found_when_task_not_found_by_id() {
    Integer taskId = 7;
    LocalDate deadline = LocalDate.of(2025, 12, 31);

    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    NotFoundException ex =
        assertThrows(NotFoundException.class,
            () -> taskService.updateDeadline(taskId, deadline));

    assertEquals("Task " + taskId + " not found", ex.getMessage());

    verify(taskRepository, times(1)).findById(taskId);
    verifyNoMoreInteractions(taskRepository);
    verifyNoInteractions(taskMapper, projectService);
  }

  @Test
  void it_updates_completed_and_calls_repository_find_by_id_and_save() {
    int taskId = 55;

    Task task = mock(Task.class);
    when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

    Task saved = mock(Task.class);
    when(taskRepository.save(task)).thenReturn(saved);

    TaskRecord record = mock(TaskRecord.class);
    when(taskMapper.toRecord(saved)).thenReturn(record);

    TaskRecord result = taskService.updateCompleted(taskId, true);

    assertSame(record, result);

    verify(taskRepository, times(1)).findById(taskId);
    verify(task, times(1)).setCompleted(true);
    verify(taskRepository, times(1)).save(task);
    verify(taskMapper, times(1)).toRecord(saved);

    verifyNoMoreInteractions(taskRepository, taskMapper, task);
    verifyNoInteractions(projectService);
  }

  @Test
  void it_throws_illegal_argument_when_task_not_found_on_update_completed() {
    int taskId = 55;
    when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class,
            () -> taskService.updateCompleted(taskId, true));

    assertEquals("Task not found ID  " + taskId, ex.getMessage());

    verify(taskRepository, times(1)).findById(taskId);
    verifyNoMoreInteractions(taskRepository);
    verifyNoInteractions(taskMapper, projectService);
  }
}
