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

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement preparedQuery = conn.prepareStatement(query))
        {
            preparedQuery.setString(1, email);
            preparedQuery.setString(2, password);
            ResultSet resultSet = preparedQuery.executeQuery();

            if (resultSet.next())
            {
                ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
                int userID = resultSet.getInt("userID");

                // Fetch genres from UserGenres
                String genreQuery = "SELECT genre FROM UserGenres WHERE userID = ?";
                PreparedStatement genreStmt = conn.prepareStatement(genreQuery);
                genreStmt.setInt(1, userID);
                ResultSet genreResults = genreStmt.executeQuery();

                while (genreResults.next())
                {
                    String genreName = genreResults.getString("genre");
                    try
                    {
                        favoriteGenres.add(Genre.GenreType.valueOf(genreName.trim()));
                    }
                    catch (IllegalArgumentException e)
                    {
                        System.err.println("Invalid genre in UserGenres: " + genreName);
                    }
                }

                currentUser = new User(
                        userID,
                        resultSet.getString("username"),
                        email,
                        password,
                        favoriteGenres
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

    public void register() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        if (User.isUsernameTaken(username))
        {
            System.out.println("Username already taken!");
            return;
        }
        System.out.print("Enter email: ");
        String email = scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();
        System.out.print("Enter favorite genres (Comma Separated): ");
        String genresInput = scanner.nextLine();

        ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
        String[] genreStrings = genresInput.split(",");

        for (String genreStr : genreStrings) {
            try {
                String formatted = genreStr.trim().toUpperCase().replace(' ', '_');
                Genre.GenreType genreType = Genre.GenreType.valueOf(formatted);
                favoriteGenres.add(genreType);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid genre: " + genreStr.trim());
            }
        }

        int userID = User.getNextUserID(); // get new ID
        User newUser = new User(userID, username, email, password, favoriteGenres);
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
