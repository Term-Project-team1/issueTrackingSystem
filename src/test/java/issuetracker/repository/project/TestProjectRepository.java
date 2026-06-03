package issuetracker.repository.project;

import issuetracker.domain.project.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestProjectRepository implements ProjectRepository {

    private final List<Project> projects = new ArrayList<>();
    private long sequence = 1L;

    @Override
    public Project save(Project project) {
        Project savedProject = new Project(
                sequence++,
                project.getName(),
                project.getCreatedDate()
        );

        projects.add(savedProject);

        return savedProject;
    }

    @Override
    public Optional<Project> findById(Long id) {
        return projects.stream()
                .filter(project -> project.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Project> findByName(String name) {
        return projects.stream()
                .filter(project -> project.getName().equals(name))
                .findFirst();
    }

    @Override
    public List<Project> findAll() {
        return projects;
    }
}