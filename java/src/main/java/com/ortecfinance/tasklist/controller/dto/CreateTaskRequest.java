package com.ortecfinance.tasklist.controller.dto;

import com.ortecfinance.tasklist.enums.TaskStatus;
import java.time.LocalDate;

public record CreateTaskRequest(
    String description,
    TaskStatus status,
    LocalDate deadline
) {

  public CreateTaskRequest {
    if (status == null) {
      status = TaskStatus.TODO;
    }
  }
}
