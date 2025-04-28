import java.sql.*;

/**
 * Database class handles the connection to the SQLite database.
 * It ensures a single connection is used throughout the application by using the Singleton design pattern.
 * Authors: Phakin Dhamsirimongkol, Pon Yimcharoen
 */
public class Database
{
    private static Database instance;   // The single instance of the Database class
    private Connection connection;  // Connection to the database
    private static final String DATABASE_URL = "jdbc:sqlite:app.db"; // The URL to connect to the SQLite database

    /**
     * Private constructor that connects to the database.
     */
    private Database()
    {
        connect();
    }

    /**
     * Returns the singleton instance of the Database class.
     * If the instance does not exist, it creates a new one.
     *
     * @return the instance of the Database class
     */
    public static Database getInstance()
    {
        if (instance == null)
        {
            instance = new Database();
        }
        return instance;
    }

    /**
     * Establishes a connection to the SQLite database.
     * Loads the JDBC driver and creates a connection.
     */
    private void connect()
    {
        try
        {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DATABASE_URL);
        }
        catch (ClassNotFoundException e)
        {
            System.err.println("SQLite JDBC driver not found.");
        }
        catch (SQLException e)
        {
            System.err.println("Failed to connect to the database: " + e.getMessage());
        }
    }

    /**
     * Returns the database connection, reconnecting if it's null or closed.
     * Ensures an active connection is always available.
     *
     * @return the current database connection
     */
    public Connection getConnection()
    {
        try
        {
            // Check if connection is null or closed, and reconnect if necessary
            if (connection == null || connection.isClosed())
            {
                connect();
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error checking connection status: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Executes a SQL query and returns the result set.
     * 
     * @param sql the SQL query to execute
     * @return the ResultSet containing query results
     * @throws SQLException if a database error occurs
     */
    public ResultSet executeQuery(String sql) throws SQLException
    {
        Statement stmt = connection.createStatement();
        return stmt.executeQuery(sql);
    }

    /**
     * Executes a SQL update (INSERT, UPDATE, or DELETE) statement.
     * 
     * @param sql the SQL statement to execute
     * @return the number of rows affected
     * @throws SQLException if a database error occurs
     */
    public int executeUpdate(String sql) throws SQLException
    {
        Statement stmt = connection.createStatement();
        return stmt.executeUpdate(sql);
    }

    /**
     * Closes the database connection if it's open and nullifies the connection object.
     * Should be called when the application is shutting down.
     */
    public void disconnect()
    {
        try {
            if (connection != null && !connection.isClosed())
            {
                connection.close();
                connection = null;
                System.out.println("Disconnected from database");
            }
        }
        catch (SQLException e) {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }
}