package com.ortecfinance.tasklist.mapper;

import com.ortecfinance.tasklist.controller.dto.TaskRecord;
import com.ortecfinance.tasklist.controller.dto.TaskResponse;
import com.ortecfinance.tasklist.model.Task;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface TaskMapper {

  TaskRecord toRecord(Task task);

  List<TaskRecord> toRecords(List<Task> tasks);

  Task toEntity(TaskRecord record);

  @Mapping(source = "project.name", target = "projectName")
  TaskResponse toResponse(Task task);

  List<TaskResponse> toResponses(List<Task> tasks);

  @Mapping(target = "project", ignore = true)
  Task toEntity(TaskResponse response);
}

