package com.firebase.controller;

import com.firebase.model.Project;
import com.firebase.model.User;
import com.firebase.service.ProjectService;
import com.firebase.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername());
        List<Project> projects = projectService.getUserProjects(user);
        model.addAttribute("user", user);
        model.addAttribute("projects", projects);
        return "dashboard/index";
    }
}
