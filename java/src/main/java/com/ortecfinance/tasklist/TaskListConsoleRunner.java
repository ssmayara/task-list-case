package com.ortecfinance.tasklist;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class TaskListConsoleRunner implements CommandLineRunner {

  private final TaskList taskList;

  public TaskListConsoleRunner(TaskList taskList) {
    this.taskList = taskList;
  }

  @Override
  public void run(String... args) {
    taskList.run();
  }
}
