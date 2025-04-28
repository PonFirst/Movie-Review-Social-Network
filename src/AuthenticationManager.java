import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * AuthenticationManager handles user authentication and registration functionalities.
 * This class follows the Singleton design pattern to ensure only one instance exists.
 */
public class AuthenticationManager
{
    private static AuthenticationManager instance; // Singleton instance
    private User currentUser; // Currently logged-in user
    private boolean loggedIn = false; // Flag to track if a user is logged in

    /**
     * Private constructor to prevent multiple instances
     */
    private AuthenticationManager()
    {
    }

    /**
     * Gets the singleton instance of AuthenticationManager
     * 
     * @return the singleton instance
     */
    public static AuthenticationManager getInstance()
    {
        if (instance == null)
        {
            instance = new AuthenticationManager();
        }
        return instance;
    }

    /**
     * Checks if a user is currently logged in
     * 
     * @return true if a user is logged in, false otherwise
     */
    public boolean isUserLoggedIn()
    {
        return loggedIn;
    }

    /**
     * Gets the currently logged-in user
     * 
     * @return the current user object or null if no user is logged in
     */
    public User getCurrentUser()
    {
        return currentUser;
    }

    /**
     * Attempts to log in a user with the provided credentials
     * 
     * @param email the user's email
     * @param password the user's password
     * @return true if login is successful, false otherwise
     */
    public boolean login(String email, String password)
    {
        // Created SQL query to validate user credentials
        String query = "SELECT * FROM users WHERE email = '" + email + "' AND password = '" + password + "'";

        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(query);

            if (resultSet.next())
            {
                ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
                int userID = resultSet.getInt("userID");

                // Fetch user's favorite genres from UserGenres table
                String genreQuery = "SELECT genre FROM UserGenres WHERE userID = " + userID;
                ResultSet genreResults = Database.getInstance().executeQuery(genreQuery);

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

                // Create user object and set login status
                currentUser = new User(userID, resultSet.getString("username"), email, password, favoriteGenres);

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

    /**
     * Handles the user registration process by collecting user information
     * and saving it to the database
     */
    public void register()
    {
        Scanner scanner = new Scanner(System.in);

        String username = promptUsername(scanner);
        String email = promptEmail(scanner);
        String password = promptPassword(scanner);
        ArrayList<Genre.GenreType> favoriteGenres = promptGenres(scanner);

        System.out.print("Confirm registration? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y"))
        {
            System.out.println("Registration cancelled.");
            return;
        }

        // Create and save the new user
        int userID = User.getNextUserID();
        User newUser = new User(userID, username, email, password, favoriteGenres);
        newUser.save();

        System.out.println("Registration successful!");
        currentUser = newUser;
        loggedIn = true;
    }

    /**
     * Logs out the current user
     */
    public void logout()
    {
        currentUser = null;
        loggedIn = false;
        System.out.println("Logged out successfully.\n");
    }

    /**
     * Prompts the user for a username and ensures it's not already taken
     * 
     * @param scanner Scanner object for input
     * @return a unique username
     */
    private String promptUsername(Scanner scanner)
    {
        while (true)
        {
            System.out.print("Enter username: ");
            String username = scanner.nextLine();
            if (!User.isUsernameTaken(username))
            {
                return username;
            }
            System.out.println("Username already taken!");
        }
    }

    /**
     * Prompts the user for a valid email address
     * 
     * @param scanner Scanner object for input
     * @return a valid email address
     */
    private String promptEmail(Scanner scanner)
    {
        while (true)
        {
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            if (InputValidator.isValidEmail(email))
            {
                return email;
            }
            System.out.println("Invalid email format.");
        }
    }

    /**
     * Prompts the user for a password
     * 
     * @param scanner Scanner object for input
     * @return the password entered by the user
     */
    private String promptPassword(Scanner scanner)
    {
        System.out.print("Enter password: ");
        return scanner.nextLine();
    }

    /**
     * Prompts the user to select their favorite genres
     * 
     * @param scanner Scanner object for input
     * @return a list of the user's favorite genres
     */
    private ArrayList<Genre.GenreType> promptGenres(Scanner scanner)
    {
        Genre.GenreType[] genres = Genre.GenreType.values();

        while (true)
        {
            System.out.println("Available genres:");
            for (int i = 0; i < genres.length; i++)
            {
                System.out.print(genres[i]);
                if (i < genres.length - 1) System.out.print(" | ");
            }
            System.out.print("\nEnter favorite genres (Comma Separated): ");
            String genresInput = scanner.nextLine();

            ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
            boolean allValid = true;

            for (String genreStr : genresInput.split(","))
            {
                try
                {
                    String formatted = genreStr.trim().toUpperCase().replace(' ', '_');
                    Genre.GenreType genre = Genre.GenreType.valueOf(formatted);
                    favoriteGenres.add(genre);
                }
                catch (IllegalArgumentException e)
                {
                    System.out.println("Invalid genre: " + genreStr.trim());
                    allValid = false;
                }
            }

            if (allValid && !favoriteGenres.isEmpty())
            {
                return favoriteGenres;
            }

            System.out.println("Please enter valid genres.");
        }
    }
}