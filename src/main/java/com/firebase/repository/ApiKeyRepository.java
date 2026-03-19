package com.firebase.repository;

import com.firebase.model.ApiKey;
import com.firebase.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    List<ApiKey> findByProjectAndActive(Project project, boolean active);
}
