package issuetracker.service.project;

import issuetracker.domain.project.Project;

import java.util.List;

public interface ProjectService {
    Project createProject(String name);

    Project findById(Long id);

    Project findByName(String name);

    List<Project> findAll();
}