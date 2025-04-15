import java.sql.*;

public class Database
{
    private static Database instance;
    private Connection connection;
    private static final String DATABASE_URL = "jdbc:sqlite:app.db";

    // Class constructor
    private Database()
    {
        connect();
    }

    public static Database getInstance()
    {
        if (instance == null)
        {
            instance = new Database();
        }
        return instance;
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connected to the database.");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found. Check if the driver JAR is in the classpath.");
        } catch (SQLException e) {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    // Class public methods
    public Connection getConnection()
    {
        return connection;
    }

    public void disconnect()
    {
        try
        {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
                System.out.println("Disconnected from database");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

}
