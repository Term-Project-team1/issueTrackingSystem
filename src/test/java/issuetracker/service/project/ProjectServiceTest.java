package issuetracker.service.project;

import issuetracker.domain.project.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        ProjectRepository projectRepository = new ProjectRepositoryImpl();
        projectService = new ProjectServiceImpl(projectRepository);
    }

    @Test
    @DisplayName("createProject: 정상 생성")
    void createProject_정상생성() {
        Project project = projectService.createProject("project1");

        assertNotNull(project);
        assertEquals("project1", project.getName());
        assertNotNull(project.getCreatedDate());
    }

    @Test
    @DisplayName("createProject: name이 비어있으면 생성 실패")
    void createProject_name이_비어있으면_생성실패() {
        assertThrows(IllegalArgumentException.class, () -> {
            projectService.createProject("");
        });
    }

    @Test
    @DisplayName("findAllProjects: 프로젝트 목록 조회")
    void findAllProjects_프로젝트목록조회() {
        projectService.createProject("project1");
        projectService.createProject("project2");

        List<Project> projects = projectService.findAll();

        assertTrue(projects.size() >= 2);
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("project1")));
        assertTrue(projects.stream().anyMatch(p -> p.getName().equals("project2")));
    }
}