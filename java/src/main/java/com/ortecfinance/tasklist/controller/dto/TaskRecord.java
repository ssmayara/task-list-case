package com.ortecfinance.tasklist.controller.dto;

import com.ortecfinance.tasklist.enums.TaskStatus;
import java.time.LocalDate;

public record TaskRecord(
    Integer id,
    String description,
    TaskStatus status,
    LocalDate deadline
) {
}