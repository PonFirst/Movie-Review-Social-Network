import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * The User class represents a user in the system with attributes like ID, username, email,
 * password, and favorite genres. It provides methods to manage user data and interact with
 * the database.
 */
public class User {
    private int userID;         // Unique identifier for the user
    private String username;    // The username of the user
    private String email;       // The email of the user
    private String password;    // The password of the user
    private ArrayList<Genre.GenreType> genres;  // The user's favorite genres

    /**
     * Constructs a User object
     * Initializes the genres list to an empty list if null is provided.
     *
     * @param userID   the unique identifier for the user
     * @param username the username of the user
     * @param email    the email address of the user
     * @param password the password of the user
     * @param genres   the list of favorite genres, or null to initialize an empty list
     */
    public User(int userID, String username, String email, String password, ArrayList<Genre.GenreType> genres) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.genres = genres != null ? genres : new ArrayList<>();
    }

    /**
     * Get the user's ID
     * @return the user ID
     */
    public int getUserID() {
        return userID;
    }

    /**
     * Get the user's username
     * @return the username
     */
    public String getUserName() {
        return username;
    }

    /**
     * Get the list of the user's favorite genres
     * @return the list of favorite genres
     */
    public ArrayList<Genre.GenreType> getFavoriteGenres() {
        return genres;
    }

    /**
     * Checks if a username is already taken in the database
     * @param username the username to check
     * @return true if the username is taken, false otherwise
     */
    public static boolean isUsernameTaken(String username) {
        try {
            // Query to check if username exists
            String query = "SELECT 1 FROM users WHERE username = '" + username + "'";
            ResultSet resultSet = Database.getInstance().executeQuery(query);
            return resultSet.next(); // returns true if username exists
        }
        catch (SQLException e) {
            System.err.println("Failed to check username: " + e.getMessage());
            return true;
        }
    }

    /**
     * Override equals method to compare users based on their userID.
     * Important for collections and comparison operations.
     * 
     * @param o the object to compare with
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getUserID() == user.getUserID();
    }

    /**
     * Override hashCode to be consistent with equals method.
     * Important for using User objects in hash-based collections.
     * 
     * @return hash code for this user
     */
    @Override
    public int hashCode() {
        return Objects.hash(getUserID());
    }

    /**
     * Saves the user's data and favorite genres to the database.
     * Inserts the user record and genres into their tables
     */
    public void save() {
        // Insert user into the database
        String query = "INSERT INTO users (userID, username, email, password) VALUES (" + 
                       this.userID + ", '" + this.username + "', '" + this.email + "', '" + this.password + "')";

        try {
            Database.getInstance().executeUpdate(query);

            // Save each genre
            for (Genre.GenreType genre : this.genres) {
                String genreQuery = "INSERT INTO UserGenres (userID, genre) VALUES (" + 
                                    this.userID + ", '" + genre.name() + "')";
                Database.getInstance().executeUpdate(genreQuery);
            }
            System.out.println("User and genres saved to database.");
        }
        catch (SQLException e) {
            System.err.println("User save failed: " + e.getMessage());
        }
    }

    /**
     * Gets the next available userID from the database
     * 
     * @return the next available userID
     */
    public static int getNextUserID() {
        // Query to find the maximum userID
        String query = "SELECT MAX(userID) AS max_id FROM users";
        try {
            ResultSet result = Database.getInstance().executeQuery(query);
            if (result.next()) {
                return result.getInt("max_id") + 1;
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to get max user ID: " + e.getMessage());
        }
        return 1;
    }

    /**
     * Gets the user's most recent review from the database
     * 
     * @return the latest Review object, or null if no reviews exist
     */
    public Review getLatestReview() {
        // Query to get the most recent review by this user
        String query = "SELECT * FROM Reviews WHERE userID = " + this.userID + " ORDER BY reviewDate DESC LIMIT 1";
        try {
            ResultSet resultSet = Database.getInstance().executeQuery(query);
            if (resultSet.next()) {
                return new Review(
                        resultSet.getInt("reviewID"),
                        resultSet.getString("content"), 
                        resultSet.getInt("rating"),
                        resultSet.getInt("userID"),
                        resultSet.getInt("movieID"),
                        resultSet.getDate("reviewDate"),
                        resultSet.getInt("likeCount")
                );
            }
        }
        catch (SQLException e) {
            System.err.println("Failed to get latest review: " + e.getMessage());
        }
        return null;
    }

    /**
     * Displays the user's profile information including username, favorite genres,
     * and their latest review if available
     */
    public void displayProfile() {
        // Print user profile information
        System.out.println("Username: " + this.getUserName());
        System.out.print("Favorite Genres: ");
        for (Genre.GenreType genre : this.getFavoriteGenres()) {
            System.out.print(genre + " ");
        }
        System.out.println("\n");
        
        // Display the latest review if available
        if (getLatestReview() != null) {
            System.out.println("Latest Review: \n" + getLatestReview());
        } else {
            System.out.println("No reviews yet.");
        }
    }
}