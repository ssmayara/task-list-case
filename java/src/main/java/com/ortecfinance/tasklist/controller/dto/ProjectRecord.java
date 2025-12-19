package com.ortecfinance.tasklist.controller.dto;

import java.util.List;

public record ProjectRecord(
    Integer id,
    String name,
    List<TaskRecord> tasks
) {

}
