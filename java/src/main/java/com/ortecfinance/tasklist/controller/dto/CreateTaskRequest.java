package com.ortecfinance.tasklist.controller.dto;

import java.time.LocalDate;

public record CreateTaskRequest(
    String description,
    boolean completed,
    LocalDate deadline
) {

}
