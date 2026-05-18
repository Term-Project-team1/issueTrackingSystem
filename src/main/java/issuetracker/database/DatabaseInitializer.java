package issuetracker.database;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseInitializer {

    public static void initialize() {

        executeSqlFile("schema.sql");

        executeSqlFile("seed.sql");

    }

    private static void executeSqlFile(String fileName) {

        try (

                Connection connection = SqliteConnectionManager.getConnection();

                Statement statement = connection.createStatement()

        ) {

            InputStream inputStream =

                    DatabaseInitializer.class.getClassLoader().getResourceAsStream(fileName);

            if (inputStream == null) {

                throw new RuntimeException(fileName + " not found");

            }

            String sql = new BufferedReader(

                    new InputStreamReader(inputStream, StandardCharsets.UTF_8)

            ).lines().collect(Collectors.joining("\n"));

            statement.executeUpdate(sql);

            System.out.println(fileName + " executed successfully.");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

}