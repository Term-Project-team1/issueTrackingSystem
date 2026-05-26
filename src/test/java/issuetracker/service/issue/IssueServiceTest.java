package issuetracker.service.issue;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import issuetracker.repository.issue.InMemoryIssueRepository;

import static org.junit.jupiter.api.Assertions.*;

class IssueServiceTest {

    private IssueService issueService;

    private Project project1;
    private Account tester1;
    private Account dev1;
    private Account pl1;

    @BeforeEach
    void setUp() {
        issueService = new IssueServiceImpl(new InMemoryIssueRepository());

        project1 = new Project(1L, "project1", LocalDateTime.now());
        tester1 = new Account(1L, "tester1", Role.TESTER);
        dev1 = new Account(2L, "dev1", Role.DEV);
        pl1 = new Account(3L, "PL1", Role.PL);
    }

    @Test
    @DisplayName("createIssue: 생성 시 status는 NEW가 된다")
    void createIssue_생성시_status가_NEW인지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        assertEquals(IssueStatus.NEW, issue.getStatus());
    }

    @Test
    @DisplayName("createIssue: 생성 시 reporter가 자동 저장된다")
    void createIssue_생성시_reporter가_자동저장되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        assertEquals(tester1, issue.getReporter());
    }

    @Test
    @DisplayName("createIssue: title이 비어있으면 생성 실패")
    void createIssue_title이_비어있으면_생성실패() {
        assertThrows(IllegalArgumentException.class, () -> {
            issueService.createIssue(
                    project1,
                    tester1,
                    "",
                    "Description exists",
                    Priority.MAJOR
            );
        });
    }

    @Test
    @DisplayName("assignIssue: 담당자 지정 시 status가 ASSIGNED로 변경된다")
    void assignIssue_담당자지정시_status가_ASSIGNED로_변경되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        issueService.assignIssue(issue.getId(), dev1, pl1);

        Issue updatedIssue = issueService.viewIssue(issue.getId());

        assertEquals(dev1, updatedIssue.getAssignee());
        assertEquals(IssueStatus.ASSIGNED, updatedIssue.getStatus());
    }

    @Test
    @DisplayName("markFixed: 처리 시 status가 FIXED이고 fixer가 저장된다")
    void markFixed_처리시_status가_FIXED이고_fixer가_저장되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        issueService.assignIssue(issue.getId(), dev1, pl1);
        issueService.markFixed(issue.getId(), dev1);

        Issue updatedIssue = issueService.viewIssue(issue.getId());

        assertEquals(IssueStatus.FIXED, updatedIssue.getStatus());
        assertEquals(dev1, updatedIssue.getFixer());
    }

    @Test
    @DisplayName("resolveIssue: FIXED 상태에서 RESOLVED로 변경된다")
    void resolveIssue_FIXED상태에서_RESOLVED로_변경되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        issueService.assignIssue(issue.getId(), dev1, pl1);
        issueService.markFixed(issue.getId(), dev1);
        issueService.resolveIssue(issue.getId(), tester1);

        Issue updatedIssue = issueService.viewIssue(issue.getId());

        assertEquals(IssueStatus.RESOLVED, updatedIssue.getStatus());
    }

    @Test
    @DisplayName("closeIssue: RESOLVED 상태에서 CLOSED로 변경된다")
    void closeIssue_RESOLVED상태에서_CLOSED로_변경되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        issueService.assignIssue(issue.getId(), dev1, pl1);
        issueService.markFixed(issue.getId(), dev1);
        issueService.resolveIssue(issue.getId(), tester1);
        issueService.closeIssue(issue.getId(), pl1);

        Issue updatedIssue = issueService.viewIssue(issue.getId());

        assertEquals(IssueStatus.CLOSED, updatedIssue.getStatus());
    }

    @Test
    @DisplayName("잘못된 상태 전이 시 예외 발생")
    void 잘못된_상태전이시_예외발생() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        assertThrows(IllegalStateException.class, () -> {
            issueService.closeIssue(issue.getId(), pl1);
        });
    }

    @Test
    @DisplayName("updateIssue: title, description, priority를 수정할 수 있다")
    void updateIssue_title_description_priority_수정되는지() {
        Issue issue = issueService.createIssue(
                project1,
                tester1,
                "Login bug",
                "Login fails with valid password",
                Priority.MAJOR
        );

        issueService.updateIssue(
                issue.getId(),
                "Updated login bug",
                "Updated login bug description",
                Priority.CRITICAL
        );

        Issue updatedIssue = issueService.viewIssue(issue.getId());

        assertEquals("Updated login bug", updatedIssue.getTitle());
        assertEquals("Updated login bug description", updatedIssue.getDescription());
        assertEquals(Priority.CRITICAL, updatedIssue.getPriority());
    }
}