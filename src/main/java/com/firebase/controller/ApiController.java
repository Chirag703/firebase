package com.firebase.controller;

import com.firebase.model.ApiKey;
import com.firebase.model.Project;
import com.firebase.model.User;
import com.firebase.repository.ApiKeyRepository;
import com.firebase.repository.ProjectRepository;
import com.firebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final ProjectRepository projectRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final UserService userService;

    @GetMapping("/public/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put("timestamp", LocalDateTime.now().toString());
        resp.put("service", "Firebase Console API");
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/v1/project/{projectId}/config")
    public ResponseEntity<Map<String, Object>> getProjectConfig(
            @PathVariable String projectId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey) {

        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "API key required"));
        }

        Optional<ApiKey> keyOpt = apiKeyRepository.findAll().stream()
            .filter(k -> k.getKeyValue().equals(apiKey) && k.isActive())
            .findFirst();

        if (keyOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid or revoked API key"));
        }

        Project project = keyOpt.get().getProject();
        if (!project.getProjectId().equals(projectId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied to this project"));
        }

        Map<String, Object> config = new HashMap<>();
        config.put("projectId", project.getProjectId());
        config.put("projectName", project.getName());
        config.put("region", project.getRegion());
        config.put("authEnabled", project.isAuthEnabled());
        config.put("firestoreEnabled", project.isFirestoreEnabled());
        config.put("storageEnabled", project.isStorageEnabled());
        config.put("messagingEnabled", project.isMessagingEnabled());
        config.put("hostingEnabled", project.isHostingEnabled());
        config.put("apiKey", apiKey);
        config.put("storageBucket", project.getProjectId() + ".appspot.com");
        config.put("databaseUrl", "https://" + project.getProjectId() + ".firebaseio.com");
        config.put("authDomain", project.getProjectId() + ".firebaseapp.com");
        return ResponseEntity.ok(config);
    }

    @PostMapping("/v1/project/{projectId}/auth/register")
    public ResponseEntity<Map<String, Object>> registerUser(
            @PathVariable String projectId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody Map<String, String> body) {

        ResponseEntity<Map<String, Object>> authCheck = validateApiKey(projectId, apiKey);
        if (authCheck != null) return authCheck;

        String email = body.getOrDefault("email", "");
        String password = body.getOrDefault("password", "");
        String name = body.getOrDefault("name", email);

        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email and password required"));
        }

        try {
            User user = userService.registerUser(name, email, password);
            Map<String, Object> resp = new HashMap<>();
            resp.put("uid", user.getId().toString());
            resp.put("email", user.getEmail());
            resp.put("name", user.getName());
            resp.put("token", generateToken(user));
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/v1/project/{projectId}/auth/login")
    public ResponseEntity<Map<String, Object>> loginUser(
            @PathVariable String projectId,
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestBody Map<String, String> body) {

        ResponseEntity<Map<String, Object>> authCheck = validateApiKey(projectId, apiKey);
        if (authCheck != null) return authCheck;

        String email = body.getOrDefault("email", "");

        try {
            User user = userService.findByEmail(email);
            Map<String, Object> resp = new HashMap<>();
            resp.put("uid", user.getId().toString());
            resp.put("email", user.getEmail());
            resp.put("name", user.getName());
            resp.put("token", generateToken(user));
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    private ResponseEntity<Map<String, Object>> validateApiKey(String projectId, String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.status(401).body(Map.of("error", "API key required"));
        }
        Optional<ApiKey> keyOpt = apiKeyRepository.findAll().stream()
            .filter(k -> k.getKeyValue().equals(apiKey) && k.isActive())
            .findFirst();
        if (keyOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid API key"));
        }
        if (!keyOpt.get().getProject().getProjectId().equals(projectId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
        }
        return null;
    }

    private String generateToken(User user) {
        return "token_" + user.getId() + "_" + System.currentTimeMillis();
    }
}
