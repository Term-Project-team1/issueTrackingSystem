package issuetracker.repository.project;

import issuetracker.database.SqliteConnectionManager;
import issuetracker.domain.project.Project;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.format.DateTimeFormatter;

public class ProjectRepositoryImpl implements ProjectRepository {

    @Override
    public Project save(Project project) {
        String sql = "INSERT INTO project (name, created_date) VALUES (?, ?)";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, project.getName());
            statement.setString(2, project.getCreatedDate().toString());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();

            if (generatedKeys.next()) {
                return new Project(
                        generatedKeys.getLong(1),
                        project.getName(),
                        project.getCreatedDate()
                );
            }

            throw new RuntimeException("Failed to save project.");

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save project.", e);
        }
    }

    @Override
    public Optional<Project> findById(Long id) {
        String sql = "SELECT id, name, created_date FROM project WHERE id = ?";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapToProject(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find project by id.", e);
        }
    }

    @Override
    public Optional<Project> findByName(String name) {
        String sql = "SELECT id, name, created_date FROM project WHERE name = ?";

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            statement.setString(1, name);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return Optional.of(mapToProject(resultSet));
            }

            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find project by name.", e);
        }
    }

    @Override
    public List<Project> findAll() {
        String sql = "SELECT id, name, created_date FROM project";

        List<Project> projects = new ArrayList<>();

        try (
                Connection connection = SqliteConnectionManager.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)
        ) {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                projects.add(mapToProject(resultSet));
            }

            return projects;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all projects.", e);
        }
    }

    private Project mapToProject(ResultSet resultSet) throws SQLException {

        return new Project(
                resultSet.getLong("id"),
                resultSet.getString("name"),
                parseDateTime(resultSet.getString("created_date"))
        );

    }

    private LocalDateTime parseDateTime(String value) {
        if (value.contains("T")) { return LocalDateTime.parse(value); }

        return LocalDateTime.parse(
                value,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        );

    }
}