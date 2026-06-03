package issuetracker.service.search;

import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.repository.issue.InMemoryIssueRepository;
import issuetracker.service.search.SearchService.IssueFilter;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceTest {

    private Connection connection;
    private SearchService searchService;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        createSchema();
        insertTestData();

        InMemoryIssueRepository issueRepo = new InMemoryIssueRepository();

        Account tester1 = new Account(1L, "tester1", Role.TESTER);
        Account dev1 = new Account(2L, "dev1", Role.DEV);
        Project project = new Project(1L, "project1", LocalDateTime.now());

        Issue issue1 = new Issue(1L, project, "로그인 버그", "로그인이 안됩니다",
                IssueStatus.NEW, Priority.MAJOR, tester1, LocalDateTime.now());
        issueRepo.save(issue1);

        Issue issue2 = new Issue(2L, project, "회원가입 오류", "이메일 중복 처리 문제",
                IssueStatus.ASSIGNED, Priority.MINOR, tester1, LocalDateTime.now());
        issue2.setAssignee(dev1);
        issueRepo.save(issue2);

        Issue issue3 = new Issue(3L, project, "검색 기능 개선", "키워드 검색이 느립니다",
                IssueStatus.NEW, Priority.CRITICAL, tester1, LocalDateTime.now());
        issueRepo.save(issue3);

        searchService = new SearchServiceImpl(issueRepo);
    }

    @AfterEach
    void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    @DisplayName("status가 NEW인 이슈 조회")
    void searchByStatus_NEW_조회() {
        List<Issue> issues = searchService.searchByStatus(IssueStatus.NEW);

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().allMatch(i -> i.getStatus() == IssueStatus.NEW));
    }

    @Test
    @DisplayName("assignee가 dev1(id=2)인 이슈 조회")
    void searchByAssignee_dev1_조회() {
        List<Issue> issues = searchService.searchByAssignee(2L);

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().allMatch(i ->
                i.getAssignee() != null && i.getAssignee().getId().equals(2L)));
    }

    @Test
    @DisplayName("reporter가 tester1(id=1)인 이슈 조회")
    void searchByReporter_tester1_조회() {
        List<Issue> issues = searchService.searchByReporter(1L);

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().allMatch(i ->
                i.getReporter().getId().equals(1L)));
    }

    @Test
    @DisplayName("키워드로 이슈 검색")
    void searchByKeyword_기능검색() {
        List<Issue> issues = searchService.searchByKeyword("로그인");

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().anyMatch(i ->
                i.getTitle().contains("로그인")
                        || (i.getDescription() != null && i.getDescription().contains("로그인"))));
    }

    @Test
    @DisplayName("복합 조건 검색 - status + assignee")
    void searchByFilter_복합조건_결과확인() {
        IssueFilter filter = new IssueFilter()
                .status(IssueStatus.ASSIGNED)
                .assigneeId(2L);
        List<Issue> issues = searchService.searchByFilter(filter);

        assertFalse(issues.isEmpty());
        assertTrue(issues.stream().allMatch(i ->
                i.getStatus() == IssueStatus.ASSIGNED
                        && i.getAssignee() != null && i.getAssignee().getId().equals(2L)));
    }

    @Test
    @DisplayName("조건 없으면 전체 반환")
    void searchByFilter_조건없으면_전체반환() {
        List<Issue> all = searchService.searchByFilter(new IssueFilter());
        assertFalse(all.isEmpty());
    }

    @Test
    @DisplayName("존재하지 않는 상태로 검색하면 빈 리스트")
    void searchByStatus_없는상태_빈리스트() {
        List<Issue> issues = searchService.searchByStatus(IssueStatus.CLOSED);
        assertTrue(issues.isEmpty());
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
        stmt.executeUpdate("INSERT INTO account(username, role) VALUES ('dev2', 'DEV')");
        stmt.executeUpdate("INSERT INTO project(name, created_date) VALUES ('project1', '2026-01-01T00:00:00')");
        stmt.executeUpdate("""
            INSERT INTO issue(project_id, title, description, status, priority, reporter_id, reported_date)
            VALUES (1, '로그인 버그', '로그인이 안됩니다', 'NEW', 'MAJOR', 1, '2026-05-01T10:00:00')
            """);
        stmt.executeUpdate("""
            INSERT INTO issue(project_id, title, description, status, priority, reporter_id, assignee_id, reported_date)
            VALUES (1, '회원가입 오류', '이메일 중복 처리 문제', 'ASSIGNED', 'MINOR', 1, 2, '2026-05-02T10:00:00')
            """);
        stmt.executeUpdate("""
            INSERT INTO issue(project_id, title, description, status, priority, reporter_id, reported_date)
            VALUES (1, '검색 기능 개선', '키워드 검색이 느립니다', 'NEW', 'CRITICAL', 1, '2026-05-03T10:00:00')
            """);
    }
}