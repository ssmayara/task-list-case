package com.ortecfinance.tasklist.mapper;

import com.ortecfinance.tasklist.controller.dto.ProjectRecord;
import com.ortecfinance.tasklist.model.Project;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;


@Mapper(
    componentModel = "spring",
    uses = TaskMapper.class,
    nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface ProjectMapper {

  ProjectRecord toRecord(Project project);

  List<ProjectRecord> toRecords(List<Project> projects);
}
