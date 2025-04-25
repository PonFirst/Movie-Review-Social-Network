import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class User
{
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
    public User(int userID, String username, String email, String password, ArrayList<Genre.GenreType> genres)
    {
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
    public int getUserID()
    {
        return userID;
    }

    /**
     * Sets the user's ID
     * @param userID the new user ID
     */
    public void setUserID(int userID)
    {
        this.userID = userID;
    }

    /**
     * Get the user's username
     * @return the username
     */
    public String getUserName()
    {
        return username;
    }

    /**
     * Sets the user's username
     * @param username the new username
     */
    public void setUserName(String username)
    {
        this.username = username;
    }

    /**
     * Get the user's email address
     * @return the email address
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Sets the user's email address
     * @param email the new email address
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * Get the user's password
     * @return the password
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Sets the user's password
     * @param password the new password
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Get the list of the user's favorite genres
     * @return the list of favorite genres
     */
    public ArrayList<Genre.GenreType> getFavoriteGenres()
    {
        return genres;
    }

    /**
     * Sets the list of the user's favorite genres
     * @param genres the new list of favorite genres
     */
    public void setFavoriteGenres(ArrayList<Genre.GenreType> genres)
    {
        this.genres = genres;
    }

    /**
     * Checks if a username is already taken in the database
     * @param username the username to check
     * @return true if the username is taken, false otherwise
     */
    public static boolean isUsernameTaken(String username)
    {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?"))
        {
            statement.setString(1, username);
            return statement.executeQuery().next(); // returns true if username exists
        }
        catch (SQLException e)
        {
            System.err.println("Failed to check username: " + e.getMessage());
            return true;
        }
    }


    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getUserID() == user.getUserID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserID());
    }

    /**
     * Saves the user's data and favorite genres to the database.
     * Inserts the user record and genres into their tables
     */
    public void save()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "INSERT INTO users (userID, username, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, this.userID);
            statement.setString(2, this.username);
            statement.setString(3, this.email);
            statement.setString(4, this.password);
            statement.executeUpdate();

            // Save each genre
            for (Genre.GenreType genre : this.genres)
            {
                String genreQuery = "INSERT INTO UserGenres (userID, genre) VALUES (?, ?)";
                try (PreparedStatement genreStatement = conn.prepareStatement(genreQuery))
                {
                    genreStatement.setInt(1, this.userID);
                    genreStatement.setString(2, genre.name());
                    genreStatement.executeUpdate();
                }
            }
            System.out.println("User and genres saved to database.");
        }
        catch (SQLException e)
        {
            System.err.println("User save failed: " + e.getMessage());
        }
    }


    public static int getNextUserID()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "SELECT MAX(userID) AS max_id FROM users";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            ResultSet result = statement.executeQuery();
            if (result.next())
            {
                return result.getInt("max_id") + 1;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get max user ID: " + e.getMessage());
        }
        return 1;
    }


    public Review getLatestReview()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "SELECT * FROM Reviews WHERE userID = ? ORDER BY reviewDate DESC LIMIT 1";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.userID);
            ResultSet resultSet = statement.executeQuery();
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
        } catch (SQLException e) {
            System.err.println("Failed to get latest review: " + e.getMessage());
        }
        return null;
    }


    public void displayProfile() {
        // Print user profile information
        // Print username and favourite genres
        System.out.println("Username: " + this.getUserName());
        System.out.print("Favorite Genres: ");
        for (Genre.GenreType genre : this.getFavoriteGenres()) {
            System.out.print(genre + " ");
        }
        System.out.println("\n");
        
        if (getLatestReview() != null) {
            System.out.println("Latest Review: \n" + getLatestReview());
        } else {
            System.out.println("No reviews yet.");
        }
    }

}
