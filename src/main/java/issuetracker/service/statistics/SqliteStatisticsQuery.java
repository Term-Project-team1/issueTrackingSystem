package issuetracker.service.statistics;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqliteStatisticsQuery implements StatisticsQuery {

    @Override
    public Map<IssueStatus, Long> countByStatus(Long projectId) {
        String sql = "SELECT status, COUNT(*) AS cnt FROM issue WHERE project_id = ? GROUP BY status";
        Map<IssueStatus, Long> result = new EnumMap<>(IssueStatus.class);
        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(IssueStatus.valueOf(rs.getString("status")), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count issues by status", e);
        }
        return result;
    }

    @Override
    public Map<Priority, Long> countByPriority(Long projectId) {
        String sql = "SELECT priority, COUNT(*) AS cnt FROM issue WHERE project_id = ? GROUP BY priority";
        Map<Priority, Long> result = new EnumMap<>(Priority.class);
        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(Priority.valueOf(rs.getString("priority")), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count issues by priority", e);
        }
        return result;
    }

    @Override
    public Map<Long, Long> countFixedIssuesByFixer(Long projectId) {
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
            throw new RuntimeException("Failed to count fixed issues by fixer", e);
        }
        return result;
    }

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
}
