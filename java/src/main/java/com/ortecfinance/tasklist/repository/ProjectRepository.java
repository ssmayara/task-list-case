package com.ortecfinance.tasklist.repository;

import com.ortecfinance.tasklist.model.Project;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Integer> {
  Optional<Project> findByName(String name);
}
