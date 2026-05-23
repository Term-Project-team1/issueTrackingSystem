package issuetracker.service.project;

import issuetracker.domain.project.Project;
import issuetracker.repository.project.ProjectRepository;
import issuetracker.repository.project.TestProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProjectServiceTest {

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        ProjectRepository projectRepository = new TestProjectRepository();
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

    @Test
    @DisplayName("findById: 특정 프로젝트 조회")
    void findById_특정프로젝트조회() {
        Project project = projectService.createProject("project1");

        Project foundProject = projectService.findById(project.getId());

        assertEquals(project.getId(), foundProject.getId());
        assertEquals("project1", foundProject.getName());
    }

    @Test
    @DisplayName("findByName: 프로젝트 이름으로 조회")
    void findByName_프로젝트이름으로조회() {
        projectService.createProject("project1");

        Project foundProject = projectService.findByName("project1");

        assertEquals("project1", foundProject.getName());
    }
}