import java.util.Scanner;

import javafx.scene.chart.PieChart.Data;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.ResultSet;

import java.util.ArrayList;



public class AuthenticationManager
{
    private static AuthenticationManager instance;
    private User currentUser;
    private boolean loggedIn = false;
    private Database database = Database.getInstance();
    private Connection connection = database.getConnection();

    private AuthenticationManager()
    {
    }

    public static AuthenticationManager getInstance()
    {
        if (instance == null)
        {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    public boolean isUserLoggedIn()
    {
        return loggedIn;
    }

    public User getCurrentUser()
    {
        return currentUser;
    }

    public boolean login(String email, String password)
    {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try
        {
            PreparedStatement preparedQuery = connection.prepareStatement(query);
            preparedQuery.setString(1, email);
            preparedQuery.setString(2, password);
            ResultSet resultSet = preparedQuery.executeQuery();

            if (resultSet.next())
            {
                currentUser = new User(
                        resultSet.getInt("userID"),
                        resultSet.getString("username"),
                        email,
                        password
                );
                loggedIn = true;
                System.out.println("Login successful. Welcome, " + currentUser.getUserName() + "!");
                return true;
            }

            return false;
        }
        catch (SQLException e)
        {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }


    public void register()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        int userID = User.getNextUserID(); // get new ID
        User newUser = new User(userID, username, email, password);
        newUser.save();

        System.out.println("Registration successful!");
        currentUser = newUser;
        loggedIn = true;
    }

    public void logout()
    {
        currentUser = null;
        loggedIn = false;
        System.out.println("Logged out successfully.");
    }


}
