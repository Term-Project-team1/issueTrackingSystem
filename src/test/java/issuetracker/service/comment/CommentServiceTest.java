package issuetracker.service.comment;

import issuetracker.repository.sql.SqlCommentRepository;
import issuetracker.repository.sql.SqlIssueRepository;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest {

    private Connection connection;
    private CommentService commentService;

    @BeforeEach
    void setUp() throws SQLException {
        // in-memory SQLite — 테스트마다 깨끗한 DB
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createSchema();
        insertTestData();

        // Repository 인터페이스에 의존 (DIP)
        SqlCommentRepository commentRepo = new SqlCommentRepository(connection);
        SqlIssueRepository issueRepo = new SqlIssueRepository(connection);
        commentService = new CommentService(commentRepo, issueRepo);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    // ── 테스트 ─────────────────────────────────────────

    @Test
    @DisplayName("댓글 정상 추가")
    void addComment_정상추가() {
        Comment result = commentService.addComment(1, 1, "버그 확인했습니다.");

        assertNotNull(result);
        assertEquals("버그 확인했습니다.", result.getContent());
        assertEquals(1, result.getIssueId());
        assertEquals(1, result.getAuthorId());
        assertNotNull(result.getCreatedDate());
    }

    @Test
    @DisplayName("빈 댓글은 예외 발생")
    void addComment_빈댓글_검증실패() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1, 1, ""));
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1, 1, "   "));
        assertThrows(IllegalArgumentException.class, () ->
                commentService.addComment(1, 1, null));
    }

    @Test
    @DisplayName("댓글 추가 시 author가 저장되는지 확인")
    void addComment_author가_저장되는지확인() {
        commentService.addComment(1, 2, "dev1이 작성한 댓글");

        List<Comment> comments = commentService.findCommentsByIssue(1);
        boolean found = comments.stream()
                .anyMatch(c -> c.getAuthorId() == 2
                        && c.getContent().equals("dev1이 작성한 댓글"));
        assertTrue(found);
    }

    @Test
    @DisplayName("이슈 상세 정보 확인 - 모든 필드 조회")
    void viewIssueDetail_모든필드확인() {
        Issue1 issue = commentService.viewIssueDetail(1);

        assertNotNull(issue);
        assertEquals(1, issue.getId());
        assertEquals("로그인 버그", issue.getTitle());
        assertEquals("로그인이 안됩니다", issue.getDescription());
        assertEquals("NEW", issue.getStatus());
        assertEquals("MAJOR", issue.getPriority());
        assertEquals(1, issue.getReporterId());
        assertNotNull(issue.getReportedDate());
    }

    @Test
    @DisplayName("이슈의 코멘트 목록 조회 - 등록 순서대로")
    void findCommentsByIssue_순서확인() {
        commentService.addComment(1, 1, "첫 번째 댓글");
        commentService.addComment(1, 2, "두 번째 댓글");

        List<Comment> comments = commentService.findCommentsByIssue(1);

        assertTrue(comments.size() >= 2);
        for (int i = 0; i < comments.size() - 1; i++) {
            assertTrue(comments.get(i).getId() < comments.get(i + 1).getId());
        }
    }

    @Test
    @DisplayName("존재하지 않는 이슈 조회 시 예외 발생")
    void viewIssueDetail_존재하지않는이슈() {
        assertThrows(IllegalArgumentException.class, () ->
                commentService.viewIssueDetail(999));
    }

    // ── DB 초기화 ───────────────────────────────────────

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
        stmt.executeUpdate("INSERT INTO project(name, created_date) VALUES ('project1', '2026-01-01')");
        stmt.executeUpdate("""
            INSERT INTO issue(project_id, title, description, status, priority, reporter_id, reported_date)
            VALUES (1, '로그인 버그', '로그인이 안됩니다', 'NEW', 'MAJOR', 1, '2026-05-01 10:00:00')
            """);
    }
}