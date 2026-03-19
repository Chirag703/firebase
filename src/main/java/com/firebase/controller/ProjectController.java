package com.firebase.controller;

import com.firebase.model.ApiKey;
import com.firebase.model.Project;
import com.firebase.model.User;
import com.firebase.service.ApiKeyService;
import com.firebase.service.ProjectService;
import com.firebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;
    private final ApiKeyService apiKeyService;

    @PostMapping("/create")
    public String createProject(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam(defaultValue = "us-central1") String region,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        try {
            Project project = projectService.createProject(name, description, region, user);
            redirectAttributes.addFlashAttribute("success", "Project '" + name + "' created successfully!");
            return "redirect:/project/" + project.getProjectId() + "/overview";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create project: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @GetMapping("/{projectId}/overview")
    public String overview(@PathVariable String projectId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        List<ApiKey> apiKeys = apiKeyService.getActiveKeys(project);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("apiKeys", apiKeys);
        model.addAttribute("activeSection", "overview");
        return "project/overview";
    }

    @GetMapping("/{projectId}/authentication")
    public String authentication(@PathVariable String projectId,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("activeSection", "authentication");
        return "project/authentication";
    }

    @GetMapping("/{projectId}/database")
    public String database(@PathVariable String projectId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("activeSection", "database");
        return "project/database";
    }

    @GetMapping("/{projectId}/storage")
    public String storage(@PathVariable String projectId,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("activeSection", "storage");
        return "project/storage";
    }

    @GetMapping("/{projectId}/messaging")
    public String messaging(@PathVariable String projectId,
                            @AuthenticationPrincipal UserDetails userDetails,
                            Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("activeSection", "messaging");
        return "project/messaging";
    }

    @GetMapping("/{projectId}/flutter-sdk")
    public String flutterSdk(@PathVariable String projectId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        List<ApiKey> apiKeys = apiKeyService.getActiveKeys(project);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("apiKeys", apiKeys);
        model.addAttribute("activeSection", "flutter-sdk");
        return "project/flutter-sdk";
    }

    @GetMapping("/{projectId}/settings")
    public String settings(@PathVariable String projectId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        model.addAttribute("user", user);
        model.addAttribute("project", project);
        model.addAttribute("activeSection", "settings");
        return "project/settings";
    }

    @PostMapping("/{projectId}/settings/update")
    public String updateSettings(@PathVariable String projectId,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        try {
            projectService.updateProject(project.getId(), name, description, user);
            redirectAttributes.addFlashAttribute("success", "Project settings updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/settings";
    }

    @PostMapping("/{projectId}/settings/services")
    public String updateServices(@PathVariable String projectId,
                                 @RequestParam(defaultValue = "false") boolean authEnabled,
                                 @RequestParam(defaultValue = "false") boolean firestoreEnabled,
                                 @RequestParam(defaultValue = "false") boolean storageEnabled,
                                 @RequestParam(defaultValue = "false") boolean messagingEnabled,
                                 @RequestParam(defaultValue = "false") boolean hostingEnabled,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        try {
            projectService.updateServices(project.getId(), authEnabled, firestoreEnabled,
                storageEnabled, messagingEnabled, hostingEnabled, user);
            redirectAttributes.addFlashAttribute("success", "Services updated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/project/" + projectId + "/settings";
    }

    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable String projectId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        try {
            projectService.deleteProject(project.getId(), user);
            redirectAttributes.addFlashAttribute("success", "Project deleted.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/{projectId}/api-keys/generate")
    public String generateApiKey(@PathVariable String projectId,
                                 @RequestParam String keyName,
                                 @RequestParam String keyType,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername());
        Project project = projectService.getProjectByProjectId(projectId, user);
        apiKeyService.generateApiKey(project, keyName, keyType);
        redirectAttributes.addFlashAttribute("success", "API key generated.");
        return "redirect:/project/" + projectId + "/overview";
    }
}
