package issuetracker.repository.recommendation;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}
