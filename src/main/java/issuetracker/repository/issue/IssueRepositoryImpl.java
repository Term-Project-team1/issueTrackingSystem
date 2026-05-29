package issuetracker.repository.issue;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.service.search.SearchService.IssueFilter;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import java.sql.*;
import java.time.LocalDateTime;

public class IssueRepositoryImpl implements IssueRepository {

    @Override
    public Issue save(Issue issue) {
        String sql = """
                INSERT INTO issue
                (project_id, title, description, status, priority, reporter_id, assignee_id, fixer_id, reported_date)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setLong(1, issue.getProject().getId());
            statement.setString(2, issue.getTitle());
            statement.setString(3, issue.getDescription());
            statement.setString(4, issue.getStatus().name());
            statement.setString(5, issue.getPriority().name());
            statement.setLong(6, issue.getReporter().getId());

            setNullableAccountId(statement, 7, issue.getAssignee());
            setNullableAccountId(statement, 8, issue.getFixer());

            statement.setString(9, issue.getReportedDate().toString());

            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();

            if (keys.next()) {
                return new Issue(
                        keys.getLong(1),
                        issue.getProject(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus(),
                        issue.getPriority(),
                        issue.getReporter(),
                        issue.getReportedDate()
                );
            }

            throw new RuntimeException("Failed to save issue.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save issue.", e);
        }
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        String sql = """
                SELECT id, project_id, title, description, status, priority,
                       reporter_id, assignee_id, fixer_id, reported_date
                FROM issue
                WHERE id = ?
                """;

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, issueId);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapToIssue(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find issue by id.", e);
        }
    }

    @Override
    public Issue update(Issue issue) {
        String sql = """
                UPDATE issue
                SET title = ?,
                    description = ?,
                    status = ?,
                    priority = ?,
                    assignee_id = ?,
                    fixer_id = ?
                WHERE id = ?
                """;

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, issue.getTitle());
            statement.setString(2, issue.getDescription());
            statement.setString(3, issue.getStatus().name());
            statement.setString(4, issue.getPriority().name());

            setNullableAccountId(statement, 5, issue.getAssignee());
            setNullableAccountId(statement, 6, issue.getFixer());

            statement.setLong(7, issue.getId());

            statement.executeUpdate();

            return issue;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update issue.", e);
        }
    }

    @Override
    public List<Issue> findByFilter(IssueFilter filter) {
        StringBuilder sql = new StringBuilder("""
            SELECT id, project_id, title, description, status, priority,
                   reporter_id, assignee_id, fixer_id, reported_date
            FROM issue
            WHERE 1 = 1
            """);

        List<Object> params = new ArrayList<>();

        if (filter.status != null) {
            sql.append(" AND status = ?");
            params.add(filter.status.name());
        }

        if (filter.assigneeId != null) {
            sql.append(" AND assignee_id = ?");
            params.add(filter.assigneeId);
        }

        if (filter.reporterId != null) {
            sql.append(" AND reporter_id = ?");
            params.add(filter.reporterId);
        }

        if (filter.keyword != null && !filter.keyword.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            String keyword = "%" + filter.keyword + "%";
            params.add(keyword);
            params.add(keyword);
        }

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            List<Issue> issues = new ArrayList<>();

            while (resultSet.next()) {
                issues.add(mapToIssue(resultSet));
            }

            return issues;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find issues by filter.", e);
        }
    }



    private Issue mapToIssue(ResultSet resultSet) throws SQLException {
        Issue issue = new Issue(
                resultSet.getLong("id"),
                new Project(resultSet.getLong("project_id"), null, null),
                resultSet.getString("title"),
                resultSet.getString("description"),
                IssueStatus.valueOf(resultSet.getString("status")),
                Priority.valueOf(resultSet.getString("priority")),
                new Account(resultSet.getLong("reporter_id"), null, null),
                LocalDateTime.parse(resultSet.getString("reported_date"))
        );

        Long assigneeId = getNullableLong(resultSet, "assignee_id");
        if (assigneeId != null) {
            issue.setAssignee(new Account(assigneeId, null, null));
        }

        Long fixerId = getNullableLong(resultSet, "fixer_id");
        if (fixerId != null) {
            issue.setFixer(new Account(fixerId, null, null));
        }

        return issue;
    }

    private void setNullableAccountId(PreparedStatement statement,
                                      int index,
                                      Account account) throws SQLException {
        if (account == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setLong(index, account.getId());
        }
    }

    private Long getNullableLong(ResultSet resultSet, String columnName) throws SQLException {
        long value = resultSet.getLong(columnName);

        if (resultSet.wasNull()) {
            return null;
        }

        return value;
    }
}