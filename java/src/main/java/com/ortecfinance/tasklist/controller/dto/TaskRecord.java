package com.ortecfinance.tasklist.controller.dto;

import java.time.LocalDate;

public record TaskRecord(
    Integer id,
    String description,
    boolean completed,
    LocalDate deadline
) {

}