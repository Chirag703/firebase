package com.firebase.repository;

import com.firebase.model.Project;
import com.firebase.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByOwnerOrderByCreatedAtDesc(User owner);
    Optional<Project> findByProjectIdAndOwner(String projectId, User owner);
    boolean existsByProjectId(String projectId);
}
