package issuetracker.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqliteConnectionManager {

    private static final String URL = "jdbc:sqlite:ITS.db";

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL);

    }

}