package com.firebase.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(unique = true)
    private String projectId;

    private String description;
    private String region = "us-central1";
    private String plan = "Spark (Free)";
    private boolean authEnabled = true;
    private boolean firestoreEnabled = false;
    private boolean storageEnabled = false;
    private boolean messagingEnabled = false;
    private boolean hostingEnabled = false;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ApiKey> apiKeys = new ArrayList<>();

    @Transient
    public String getProjectIdDisplay() {
        return projectId != null ? projectId : name.toLowerCase().replaceAll("\\s+", "-");
    }
}
