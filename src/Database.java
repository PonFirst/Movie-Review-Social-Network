import java.sql.*;

public class Database
{
    private static Database instance;
    private Connection connection;
    private static String databaseURL = "jdbc:sqlite:app.db";

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

    public void connect()
    {
        try
        {
            connection = DriverManager.getConnection(databaseURL);
            System.out.println("Connected to database");
        }
        catch (SQLException e)
        {
            System.err.println("Connection failed: " + e.getMessage());
        }
    }

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
