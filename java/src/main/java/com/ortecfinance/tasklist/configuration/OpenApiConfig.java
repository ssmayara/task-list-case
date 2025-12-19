package com.ortecfinance.tasklist.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI taskListCaseOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Task list case API")
            .description("API for task list")
            .version("v1"));
  }

}
