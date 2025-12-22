package com.ortecfinance.tasklist.controller.dto;

import java.time.LocalDate;

public record TaskResponse(
    Long id,
    String description,
    LocalDate deadline,
    String projectName
) {

}
