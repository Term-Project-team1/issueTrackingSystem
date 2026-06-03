package issuetracker.repository.statistics;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import issuetracker.domain.issue.IssueStatus;
import issuetracker.domain.issue.Priority;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsRepositoryImpl implements StatisticsRepository {

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
    public Map<LocalDate, Long> countByDay(Long projectId, YearMonth month) {
        String sql =
                "SELECT DATE(reported_date) AS d, COUNT(*) AS cnt FROM issue " +
                "WHERE project_id = ? AND strftime('%Y-%m', reported_date) = ? " +
                "GROUP BY d";
        String monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Map<LocalDate, Long> result = new HashMap<>();
        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            statement.setString(2, monthStr);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(LocalDate.parse(rs.getString("d")), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count issues by day", e);
        }
        return result;
    }

    @Override
    public Map<YearMonth, Long> countByMonth(Long projectId, int year) {
        String sql =
                "SELECT strftime('%Y-%m', reported_date) AS m, COUNT(*) AS cnt FROM issue " +
                "WHERE project_id = ? AND strftime('%Y', reported_date) = ? " +
                "GROUP BY m";
        Map<YearMonth, Long> result = new HashMap<>();
        try (Connection connection = SqliteConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, projectId);
            statement.setString(2, String.valueOf(year));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    result.put(YearMonth.parse(rs.getString("m")), rs.getLong("cnt"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count issues by month", e);
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
