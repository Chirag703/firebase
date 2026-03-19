package com.firebase.service;

import com.firebase.model.ApiKey;
import com.firebase.model.Project;
import com.firebase.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;

    public ApiKey generateApiKey(Project project, String keyName, String keyType) {
        ApiKey apiKey = new ApiKey();
        apiKey.setProject(project);
        apiKey.setKeyName(keyName);
        apiKey.setKeyType(keyType);
        apiKey.setKeyValue(generateKeyValue());
        return apiKeyRepository.save(apiKey);
    }

    public List<ApiKey> getActiveKeys(Project project) {
        return apiKeyRepository.findByProjectAndActive(project, true);
    }

    public void revokeKey(Long keyId) {
        apiKeyRepository.findById(keyId).ifPresent(key -> {
            key.setActive(false);
            apiKeyRepository.save(key);
        });
    }

    private String generateKeyValue() {
        return "fb-" + UUID.randomUUID().toString().replace("-", "")
            + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }
}
