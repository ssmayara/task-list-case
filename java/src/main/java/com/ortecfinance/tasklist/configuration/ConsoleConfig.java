package com.ortecfinance.tasklist.configuration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConsoleConfig {

  @Bean
  public BufferedReader consoleReader() {
    return new BufferedReader(new InputStreamReader(System.in));
  }

  @Bean
  public PrintWriter consoleWriter() {
    return new PrintWriter(System.out, true);
  }
}
