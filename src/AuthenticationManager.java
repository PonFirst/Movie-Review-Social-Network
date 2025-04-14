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
        // ToDo

        String query = "SELECT * FROM users WHERE email = ? AND password = ?";

        try {
            PreparedStatement preparedQuery = connection.prepareStatement(query);
            preparedQuery.setString(1, email);
            preparedQuery.setString(2, password);
            ResultSet resultSet = preparedQuery.executeQuery();

            return resultSet.next();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
    }

    public void register(User newUser)
    {
        currentUser = newUser;
        loggedIn = true;
        System.out.println("Registration successful!");
    }

    public void logout()
    {
        currentUser = null;
        loggedIn = false;
        System.out.println("Logged out successfully.");
    }


}
