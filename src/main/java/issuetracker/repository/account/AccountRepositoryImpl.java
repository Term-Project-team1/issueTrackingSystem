package issuetracker.repository.account;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.account.Account;
import issuetracker.domain.account.Role;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Account save(Account account) {
        String sql = "INSERT INTO account (username, role) VALUES (?, ?)";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, account.getUsername());
            statement.setString(2, account.getRole().name());
            statement.executeUpdate();

            ResultSet keys = statement.getGeneratedKeys();

            if (keys.next()) {
                return new Account(
                        keys.getLong(1),
                        account.getUsername(),
                        account.getRole()
                );
            }

            throw new RuntimeException("Failed to save account.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save account.", e);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        String sql = "SELECT id, username, role FROM account WHERE id = ?";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapToAccount(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by id.", e);
        }
    }

    @Override
    public Optional<Account> findByUsername(String username) {
        String sql = "SELECT id, username, role FROM account WHERE username = ?";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapToAccount(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account by username.", e);
        }
    }

    @Override
    public List<Account> findByRole(Role role) {
        String sql = "SELECT id, username, role FROM account WHERE role = ?";

        List<Account> accounts = new ArrayList<>();

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, role.name());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                accounts.add(mapToAccount(resultSet));
            }

            return accounts;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find accounts by role.", e);
        }
    }

    @Override
    public List<Account> findAll() {
        String sql = "SELECT id, username, role FROM account";

        List<Account> accounts = new ArrayList<>();

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                accounts.add(mapToAccount(resultSet));
            }

            return accounts;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all accounts.", e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }

    private Account mapToAccount(ResultSet resultSet) throws SQLException {
        return new Account(
                resultSet.getLong("id"),
                resultSet.getString("username"),
                Role.valueOf(resultSet.getString("role"))
        );
    }
}