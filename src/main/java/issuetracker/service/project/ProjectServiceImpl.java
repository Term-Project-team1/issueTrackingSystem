package issuetracker.service.project;

import issuetracker.domain.project.Project;
import issuetracker.repository.project.ProjectRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @Override
    public Project createProject(String name) {
        validateName(name);

        Project project = new Project(null, name, LocalDateTime.now());
        return projectRepository.save(project);
    }

    @Override
    public Project findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Project id must not be null.");
        }

        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
    }

    @Override
    public Project findByName(String name) {
        validateName(name);

        return projectRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Project not found."));
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Project name must not be empty.");
        }
    }
}