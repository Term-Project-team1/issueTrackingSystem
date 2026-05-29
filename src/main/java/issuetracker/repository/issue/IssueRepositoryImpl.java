package issuetracker.repository.issue;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.issue.Issue;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import issuetracker.domain.project.Project;
import issuetracker.service.search.SearchService;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import issuetracker.domain.account.Role;



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
                Issue savedIssue = new Issue(
                        keys.getLong(1),
                        issue.getProject(),
                        issue.getTitle(),
                        issue.getDescription(),
                        issue.getStatus(),
                        issue.getPriority(),
                        issue.getReporter(),
                        issue.getReportedDate()
                );

                savedIssue.setAssignee(issue.getAssignee());
                savedIssue.setFixer(issue.getFixer());

                return savedIssue;
            }

            throw new RuntimeException("Failed to save issue.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save issue.", e);
        }
    }

    @Override
    public Optional<Issue> findById(Long issueId) {
        String sql = """
        SELECT
            i.id,
            i.title,
            i.description,
            i.status,
            i.priority,
            i.reported_date,

            p.id AS project_id,
            p.name AS project_name,
            p.created_date AS project_created_date,

            reporter.id AS reporter_id,
            reporter.username AS reporter_username,
            reporter.role AS reporter_role,

            assignee.id AS assignee_id,
            assignee.username AS assignee_username,
            assignee.role AS assignee_role,

            fixer.id AS fixer_id,
            fixer.username AS fixer_username,
            fixer.role AS fixer_role

        FROM issue i
        JOIN project p ON i.project_id = p.id
        JOIN account reporter ON i.reporter_id = reporter.id
        LEFT JOIN account assignee ON i.assignee_id = assignee.id
        LEFT JOIN account fixer ON i.fixer_id = fixer.id
        WHERE i.id = ?
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
    public List<Issue> findAll() {
        String sql = """
            SELECT
                i.id,
                i.title,
                i.description,
                i.status,
                i.priority,
                i.reported_date,

                p.id AS project_id,
                p.name AS project_name,
                p.created_date AS project_created_date,

                reporter.id AS reporter_id,
                reporter.username AS reporter_username,
                reporter.role AS reporter_role,

                assignee.id AS assignee_id,
                assignee.username AS assignee_username,
                assignee.role AS assignee_role,

                fixer.id AS fixer_id,
                fixer.username AS fixer_username,
                fixer.role AS fixer_role

            FROM issue i
            JOIN project p ON i.project_id = p.id
            JOIN account reporter ON i.reporter_id = reporter.id
            LEFT JOIN account assignee ON i.assignee_id = assignee.id
            LEFT JOIN account fixer ON i.fixer_id = fixer.id
            """;

        List<Issue> issues = new ArrayList<>();

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                issues.add(mapToIssue(resultSet));
            }

            return issues;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all issues.", e);
        }
    }

    @Override
    public List<Issue> findByFilter(SearchService.IssueFilter filter) {
        StringBuilder sql = new StringBuilder("""
        SELECT
            i.id, i.title, i.description, i.status, i.priority, i.reported_date,
            p.id AS project_id, p.name AS project_name, p.created_date AS project_created_date,
            reporter.id AS reporter_id, reporter.username AS reporter_username, reporter.role AS reporter_role,
            assignee.id AS assignee_id, assignee.username AS assignee_username, assignee.role AS assignee_role,
            fixer.id AS fixer_id, fixer.username AS fixer_username, fixer.role AS fixer_role
        FROM issue i
        JOIN project p ON i.project_id = p.id
        JOIN account reporter ON i.reporter_id = reporter.id
        LEFT JOIN account assignee ON i.assignee_id = assignee.id
        LEFT JOIN account fixer ON i.fixer_id = fixer.id
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

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                issues.add(mapToIssue(resultSet));
            }

            return issues;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find issues by filter.", e);
        }
    }

    private Issue mapToIssue(ResultSet resultSet) throws SQLException {
        Project project = new Project(
                resultSet.getLong("project_id"),
                resultSet.getString("project_name"),
                LocalDateTime.parse(resultSet.getString("project_created_date"))
        );

        Account reporter = new Account(
                resultSet.getLong("reporter_id"),
                resultSet.getString("reporter_username"),
                Role.valueOf(resultSet.getString("reporter_role"))
        );

        Issue issue = new Issue(
                resultSet.getLong("id"),
                project,
                resultSet.getString("title"),
                resultSet.getString("description"),
                IssueStatus.valueOf(resultSet.getString("status")),
                Priority.valueOf(resultSet.getString("priority")),
                reporter,
                LocalDateTime.parse(resultSet.getString("reported_date"))
        );

        Long assigneeId = getNullableLong(resultSet, "assignee_id");
        if (assigneeId != null) {
            Account assignee = new Account(
                    assigneeId,
                    resultSet.getString("assignee_username"),
                    Role.valueOf(resultSet.getString("assignee_role"))
            );
            issue.setAssignee(assignee);
        }

        Long fixerId = getNullableLong(resultSet, "fixer_id");
        if (fixerId != null) {
            Account fixer = new Account(
                    fixerId,
                    resultSet.getString("fixer_username"),
                    Role.valueOf(resultSet.getString("fixer_role"))
            );
            issue.setFixer(fixer);
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