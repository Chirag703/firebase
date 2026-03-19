package com.firebase.service;

import com.firebase.model.Project;
import com.firebase.model.User;
import com.firebase.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ApiKeyService apiKeyService;

    public List<Project> getUserProjects(User user) {
        return projectRepository.findByOwnerOrderByCreatedAtDesc(user);
    }

    @Transactional
    public Project createProject(String name, String description, String region, User owner) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setRegion(region);
        project.setOwner(owner);
        project.setProjectId(generateProjectId(name));
        Project saved = projectRepository.save(project);
        apiKeyService.generateApiKey(saved, "Web API Key", "web");
        apiKeyService.generateApiKey(saved, "Server API Key", "server");
        return saved;
    }

    public Project getProject(Long id, User owner) {
        return projectRepository.findById(id)
            .filter(p -> p.getOwner().getId().equals(owner.getId()))
            .orElseThrow(() -> new IllegalArgumentException("Project not found"));
    }

    public Project getProjectByProjectId(String projectId, User owner) {
        return projectRepository.findByProjectIdAndOwner(projectId, owner)
            .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
    }

    @Transactional
    public Project updateProject(Long id, String name, String description, User owner) {
        Project project = getProject(id, owner);
        project.setName(name);
        project.setDescription(description);
        return projectRepository.save(project);
    }

    @Transactional
    public Project updateServices(Long id, boolean auth, boolean firestore,
                                   boolean storage, boolean messaging, boolean hosting, User owner) {
        Project project = getProject(id, owner);
        project.setAuthEnabled(auth);
        project.setFirestoreEnabled(firestore);
        project.setStorageEnabled(storage);
        project.setMessagingEnabled(messaging);
        project.setHostingEnabled(hosting);
        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id, User owner) {
        Project project = getProject(id, owner);
        projectRepository.delete(project);
    }

    private String generateProjectId(String name) {
        String base = name.toLowerCase().replaceAll("[^a-z0-9]", "-").replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
        if (base.isEmpty()) base = "project";
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String candidate = base + "-" + suffix;
        while (projectRepository.existsByProjectId(candidate)) {
            candidate = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return candidate;
    }
}
