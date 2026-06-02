package issuetracker.controller;

import issuetracker.domain.project.Project;
import issuetracker.service.project.ProjectService;

import java.util.List;

public class ProjectController {
    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    public Project createProject(String name) {
        return projectService.createProject(name);
    }

    public Project findById(Long id) {
        return projectService.findById(id);
    }

    public Project findByName(String name) {
        return projectService.findByName(name);
    }

    public List<Project> findAll() {
        return projectService.findAll();
    }
}
