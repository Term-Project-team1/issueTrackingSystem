package issuetracker.repository.recommendation;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecommendationRepositoryImpl implements RecommendationRepository {

    @Override
    public List<Account> findDevelopers() {
        String sql = "SELECT id, username, role FROM account WHERE role = ?";
        List<Account> result = new ArrayList<>();

        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, Role.DEV.name());

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(new Account(
                            rs.getLong("id"),
                            rs.getString("username"),
                            Role.valueOf(rs.getString("role"))
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load developers", e);
        }

        return result;
    }

    @Override
    public Map<Long, Long> countFixedIssuesByDeveloper(Long projectId) {
        String sql =
                "SELECT fixer_id, COUNT(*) AS cnt FROM issue " +
                        "WHERE project_id = ? AND fixer_id IS NOT NULL " +
                        "GROUP BY fixer_id";

        Map<Long, Long> result = new HashMap<>();

        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, projectId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getLong("fixer_id"), rs.getLong("cnt"));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to count fixed issues by developer", e);
        }

        return result;
    }

    @Override
    public List<Issue> findSolvedIssues(Long projectId) {
        String sql = """
                SELECT id, project_id, title, description, status, priority,
                       reporter_id, assignee_id, fixer_id, reported_date
                FROM issue
                WHERE project_id = ?
                  AND status IN ('RESOLVED', 'CLOSED')
                  AND fixer_id IS NOT NULL
                """;

        List<Issue> result = new ArrayList<>();

        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, projectId);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.add(mapToIssue(rs));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load solved issues", e);
        }

        return result;
    }

    private Issue mapToIssue(ResultSet rs) throws SQLException {
        Issue issue = new Issue(
                rs.getLong("id"),
                new Project(rs.getLong("project_id"), null, null),
                rs.getString("title"),
                rs.getString("description"),
                IssueStatus.valueOf(rs.getString("status")),
                Priority.valueOf(rs.getString("priority")),
                new Account(rs.getLong("reporter_id"), null, null),
                parseDateTime(rs.getString("reported_date"))
        );

        long assigneeId = rs.getLong("assignee_id");
        if (!rs.wasNull()) {
            issue.setAssignee(new Account(assigneeId, null, null));
        }

        long fixerId = rs.getLong("fixer_id");
        if (!rs.wasNull()) {
            issue.setFixer(new Account(fixerId, null, null));
        }

        return issue;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value.contains("T")) {
            return LocalDateTime.parse(value);
        }

        return LocalDateTime.parse(
                value,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );
    }
}