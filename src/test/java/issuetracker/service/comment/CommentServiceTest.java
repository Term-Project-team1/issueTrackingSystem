package issuetracker.service.comment;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.comment.Comment;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.repository.comment.CommentRepositoryImpl;
import issuetracker.repository.issue.InMemoryIssueRepository;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    private Connection connection;
    private CommentService commentService;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createSchema();
        insertTestData();

        InMemoryIssueRepository issueRepo = new InMemoryIssueRepository();
        Account reporter = new Account(1L, "tester1", Role.TESTER);
        Project project = new Project(1L, "project1", LocalDateTime.now());
        Issue testIssue = new Issue(
                1L, project, "로그인 버그", "로그인이 안됩니다",
                IssueStatus.NEW, Priority.MAJOR, reporter, LocalDateTime.now()
        );
        issueRepo.save(testIssue);

        CommentRepositoryImpl commentRepo = new CommentRepositoryImpl(connection);
        commentService = new CommentService(commentRepo, issueRepo);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("댓글 정상 추가")
    void addComment_정상추가() {
        Comment result = commentService.addComment(1L, 1L, "버그 확인했습니다.");

        assertNotNull(result);
        assertEquals("버그 확인했습니다.", result.getContent());
        assertEquals(1L, result.getIssue().getId());
        assertEquals(1L, result.getAuthor().getId());
        assertNotNull(result.getCreatedDate());
    }

    @Test
    @DisplayName("빈 댓글은 예외 발생")
    void addComment_빈댓글_검증실패() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1L, 1L, ""));
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1L, 1L, "   "));
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1L, 1L, null));
    }

    @Test
    @DisplayName("댓글 추가 시 author가 저장되는지 확인")
    void addComment_author가_저장되는지확인() {
        commentService.addComment(1L, 2L, "dev1이 작성한 댓글");

        List<Comment> comments = commentService.findCommentsByIssue(1L);
        boolean found = comments.stream()
                .anyMatch(c -> c.getAuthor().getId().equals(2L)
                        && c.getContent().equals("dev1이 작성한 댓글"));
        assertTrue(found);
    }

    @Test
    @DisplayName("이슈 상세 정보 확인 - 모든 필드 조회")
    void viewIssueDetail_모든필드확인() {
        Issue issue = commentService.viewIssueDetail(1L);

        assertNotNull(issue);
        assertEquals(1L, issue.getId());
        assertEquals("로그인 버그", issue.getTitle());
        assertEquals("로그인이 안됩니다", issue.getDescription());
        assertEquals(IssueStatus.NEW, issue.getStatus());
        assertEquals(Priority.MAJOR, issue.getPriority());
        assertEquals(1L, issue.getReporter().getId());
        assertNotNull(issue.getReportedDate());
    }

    @Test
    @DisplayName("이슈의 코멘트 목록 조회 - 등록 순서대로")
    void findCommentsByIssue_순서확인() {
        commentService.addComment(1L, 1L, "첫 번째 댓글");
        commentService.addComment(1L, 2L, "두 번째 댓글");

        List<Comment> comments = commentService.findCommentsByIssue(1L);

        assertTrue(comments.size() >= 2);
        for (int i = 0; i < comments.size() - 1; i++) {
            assertTrue(comments.get(i).getId() < comments.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("존재하지 않는 이슈 조회 시 예외 발생")
    void viewIssueDetail_존재하지않는이슈() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.viewIssueDetail(999L));
    }

    private void createSchema() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS account (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                role TEXT NOT NULL
            )""");
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS project (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                created_date TEXT NOT NULL
            )""");
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS issue (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                project_id INTEGER NOT NULL,
                title TEXT NOT NULL,
                description TEXT,
                status TEXT NOT NULL,
                priority TEXT NOT NULL,
                reporter_id INTEGER NOT NULL,
                assignee_id INTEGER,
                fixer_id INTEGER,
                reported_date TEXT NOT NULL
            )""");
        stmt.executeUpdate("""
            CREATE TABLE IF NOT EXISTS comment (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                issue_id INTEGER NOT NULL,
                author_id INTEGER NOT NULL,
                content TEXT NOT NULL,
                created_date TEXT NOT NULL
            )""");
    }

    private void insertTestData() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.executeUpdate("INSERT INTO account(username, role) VALUES ('tester1', 'TESTER')");
        stmt.executeUpdate("INSERT INTO account(username, role) VALUES ('dev1', 'DEV')");
        stmt.executeUpdate("INSERT INTO project(name, created_date) VALUES ('project1', '2026-01-01T00:00:00')");
        stmt.executeUpdate("""
            INSERT INTO issue(project_id, title, description, status, priority, reporter_id, reported_date)
            VALUES (1, '로그인 버그', '로그인이 안됩니다', 'NEW', 'MAJOR', 1, '2026-05-01T10:00:00')
            """);
    }
}