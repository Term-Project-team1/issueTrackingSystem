package issuetracker.repository.sql;

import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.model.IssueFilter;
import issuetracker.repository.issue.IssueRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class SqlIssueRepository implements IssueRepository {

    private final Connection connection;

    public SqlIssueRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        String sql = """
            SELECT i.*,
                   r.id as r_id, r.username as r_username,
                   a.id as a_id, a.username as a_username,
                   f.id as f_id, f.username as f_username,
                   p.id as p_id, p.name as p_name
            FROM issue i
            JOIN account r ON i.reporter_id = r.id
            JOIN project p ON i.project_id = p.id
            LEFT JOIN account a ON i.assignee_id = a.id
            LEFT JOIN account f ON i.fixer_id = f.id
            WHERE i.id = ?
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, issueId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return Optional.of(mapIssue(rs));
        } catch (SQLException e) {
            throw new RuntimeException("이슈 조회 실패", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Issue> findByFilter(IssueFilter filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT i.*,
                   r.id as r_id, r.username as r_username,
                   a.id as a_id, a.username as a_username,
                   f.id as f_id, f.username as f_username,
                   p.id as p_id, p.name as p_name
            FROM issue i
            JOIN account r ON i.reporter_id = r.id
            JOIN project p ON i.project_id = p.id
            LEFT JOIN account a ON i.assignee_id = a.id
            LEFT JOIN account f ON i.fixer_id = f.id
            WHERE 1=1
            """);
        List<Object> params = new ArrayList<>();

        if (filter.status != null) {
            sql.append(" AND i.status = ?");
            params.add(filter.status.name());
        }
        if (filter.assigneeId != null) {
            sql.append(" AND i.assignee_id = ?");
            params.add(filter.assigneeId);
        }
        if (filter.reporterId != null) {
            sql.append(" AND i.reporter_id = ?");
            params.add(filter.reporterId);
        }
        if (filter.keyword != null && !filter.keyword.isBlank()) {
            sql.append(" AND (i.title LIKE ? OR i.description LIKE ?)");
            params.add("%" + filter.keyword + "%");
            params.add("%" + filter.keyword + "%");
        }

        List<Issue> issues = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) pstmt.setObject(i + 1, params.get(i));
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) issues.add(mapIssue(rs));
        } catch (SQLException e) {
            throw new RuntimeException("필터 검색 실패", e);
        }
        return issues;
    }

    private Issue mapIssue(ResultSet rs) throws SQLException {
        Account reporter = new Account();
        reporter.setId(rs.getLong("r_id"));
        reporter.setUsername(rs.getString("r_username"));

        Project project = new Project();
        project.setId(rs.getLong("p_id"));
        project.setName(rs.getString("p_name"));

        Issue issue = new Issue();
        issue.setId(rs.getLong("id"));
        issue.setProject(project);
        issue.setTitle(rs.getString("title"));
        issue.setDescription(rs.getString("description"));
        issue.setStatus(IssueStatus.valueOf(rs.getString("status")));
        issue.setPriority(Priority.valueOf(rs.getString("priority")));
        issue.setReporter(reporter);
        issue.setReportedDate(LocalDateTime.parse(
                rs.getString("reported_date").replace(" ", "T")));

        if (rs.getString("a_username") != null) {
            Account assignee = new Account();
            assignee.setId(rs.getLong("a_id"));
            assignee.setUsername(rs.getString("a_username"));
            issue.setAssignee(assignee);
        }
        if (rs.getString("f_username") != null) {
            Account fixer = new Account();
            fixer.setId(rs.getLong("f_id"));
            fixer.setUsername(rs.getString("f_username"));
            issue.setFixer(fixer);
        }
        return issue;
    }
}