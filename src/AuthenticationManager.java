import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class AuthenticationManager
{
    private static AuthenticationManager instance;
    private User currentUser;
    private boolean loggedIn = false;

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
        // Changed to use Database.executeQuery instead of direct connection
        String query = "SELECT * FROM users WHERE email = '" + email + "' AND password = '" + password + "'";

        try 
        {
            ResultSet resultSet = Database.getInstance().executeQuery(query);

            if (resultSet.next())
            {
                ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
                int userID = resultSet.getInt("userID");

                // Fetch genres from UserGenres
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

    // Other methods remain unchanged
    public void register()
    {
        Scanner scanner = new Scanner(System.in);

        String username = promptUsername(scanner);
        String email = promptEmail(scanner);
        String password = promptPassword(scanner);
        ArrayList<Genre.GenreType> favoriteGenres = promptGenres(scanner);

        System.out.print("Confirm registration? (y/n): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (!confirm.equals("y")) {
            System.out.println("Registration cancelled.");
            return;
        }

        int userID = User.getNextUserID();
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
        System.out.println("Logged out successfully.\n");
    }

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

    private String promptEmail(Scanner scanner)
    {
        while (true) {
            System.out.print("Enter email: ");
            String email = scanner.nextLine();
            if (InputValidator.isValidEmail(email))
            {
                return email;
            }
            System.out.println("Invalid email format.");
        }
    }

    private String promptPassword(Scanner scanner)
    {
        System.out.print("Enter password: ");
        return scanner.nextLine();
    }

    private ArrayList<Genre.GenreType> promptGenres(Scanner scanner)
    {
        Genre.GenreType[] genres = Genre.GenreType.values();

        while (true) {
            System.out.println("Available genres:");
            for (int i = 0; i < genres.length; i++) {
                System.out.print(genres[i]);
                if (i < genres.length - 1) System.out.print(" | ");
            }
            System.out.print("\nEnter favorite genres (Comma Separated): ");
            String genresInput = scanner.nextLine();

            ArrayList<Genre.GenreType> favoriteGenres = new ArrayList<>();
            boolean allValid = true;

            for (String genreStr : genresInput.split(",")) {
                try {
                    String formatted = genreStr.trim().toUpperCase().replace(' ', '_');
                    Genre.GenreType genre = Genre.GenreType.valueOf(formatted);
                    favoriteGenres.add(genre);
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid genre: " + genreStr.trim());
                    allValid = false;
                }
            }

            if (allValid && !favoriteGenres.isEmpty()) {
                return favoriteGenres;
            }

            System.out.println("Please enter valid genres.");
        }
    }
}