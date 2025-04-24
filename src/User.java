import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

public class User
{
    private int userID;
    private String username;
    private String email;
    private String password;
    private ArrayList<Genre.GenreType> genres;

    public User(int userID, String username, String email, String password, ArrayList<Genre.GenreType> genres)
    {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.genres = genres != null ? genres : new ArrayList<>();
    }


    public int getUserID()
    {
        return userID;
    }

    public void setUserID(int userID)
    {
        this.userID = userID;
    }

    public String getUserName()
    {
        return username;
    }

    public void setUserName(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public ArrayList<Genre.GenreType> getFavoriteGenres()
    {
        return genres;
    }

    public void setFavoriteGenres(ArrayList<Genre.GenreType> genres)
    {
        this.genres = genres;
    }

    public static boolean isUsernameTaken(String username)
    {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM users WHERE username = ?"))
        {
            stmt.setString(1, username);
            return stmt.executeQuery().next(); // returns true if username exists

        } catch (SQLException e) {
            System.err.println("Failed to check username: " + e.getMessage());
            return true;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return getUserID() == user.getUserID();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserID());
    }


    public void save()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "INSERT INTO users (userID, username, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.userID);
            statement.setString(2, this.username);
            statement.setString(3, this.email);
            statement.setString(4, this.password);
            statement.executeUpdate();

            // Save each genre
            for (Genre.GenreType genre : this.genres) {
                String genreQuery = "INSERT INTO UserGenres (userID, genre) VALUES (?, ?)";
                try (PreparedStatement genreStatement = conn.prepareStatement(genreQuery)) {
                    genreStatement.setInt(1, this.userID);
                    genreStatement.setString(2, genre.name());
                    genreStatement.executeUpdate();
                }
            }
            System.out.println("User and genres saved to database.");
        } catch (SQLException e) {
            System.err.println("User save failed: " + e.getMessage());
        }
    }



    public static ArrayList<User> load()
    {
        ArrayList<User> users = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();
        String query = "SELECT * FROM users";

        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                int userID = resultSet.getInt("userID");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");

                // Load genres for the user
                ArrayList<Genre.GenreType> genres = new ArrayList<>();
                String genreQuery = "SELECT genre FROM UserGenres WHERE userID = ?";
                try (PreparedStatement genreStatement = connection.prepareStatement(genreQuery))
                {
                    genreStatement.setInt(1, userID);
                    ResultSet genreResultSet = genreStatement.executeQuery();
                    while (genreResultSet.next())
                    {
                        try
                        {
                            genres.add(Genre.GenreType.valueOf(genreResultSet.getString("genre")));
                        }
                        catch (IllegalArgumentException e)
                        {
                            System.err.println("Invalid genre in database.");
                        }
                    }
                }

                users.add(new User(userID, username, email, password, genres));
            }
        }
        catch (SQLException exception)
        {
            System.err.println("Failed to load users: " + exception.getMessage());
        }

        return users;
    }


    public static int getNextUserID()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "SELECT MAX(userID) AS max_id FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(query))
        {
            ResultSet result = stmt.executeQuery();
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
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.userID);
            ResultSet resultSet = stmt.executeQuery();
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
    
 /* 
    private void printLatestReviews(User targetUser) {
        Connection conn = Database.getInstance().getConnection();
        
        // Query for latest reviews
        String query = "SELECT " +
        "r.reviewID, " +
        "r.movieID, " +
        "r.content, " +
        "r.rating, " +
        "r.reviewDate, " +
        "r.likeCount, " +
        "m.title " +
        "FROM Reviews r " +
        "JOIN Movies m ON r.movieID = m.id " +
        "WHERE r.userID = ? " +
        "ORDER BY r.reviewDate DESC " +
        "LIMIT 3";
    
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, targetUser.getUserID());
            ResultSet resultSet = statement.executeQuery();
            
            System.out.println("\nLatest Reviews:");
            
            boolean hasReviews = false;
            while (resultSet.next()) {
                hasReviews = true;
                
                int reviewId = resultSet.getInt("reviewID");
                int movieID = resultSet.getInt("movieID");
                String text = resultSet.getString("content");
                int rating = resultSet.getInt("rating");
                Date reviewDate = resultSet.getDate("reviewDate");
                int likeCount = resultSet.getInt("likeCount");
                String movieTitle = resultSet.getString("title");
                
                // Truncate text to first 50 characters
                String truncatedText = text.length() > 50 
                    ? text.substring(0, 50) + "..." 
                    : text;
                
                // Print review details
                System.out.println("---");
                System.out.println("Movie: " + movieTitle);
                System.out.println("Rating: " + rating);
                System.out.println("Review Date: " + reviewDate);
                System.out.println("Likes: " + likeCount);
                System.out.println("Review: " + truncatedText);
            }
            
            if (!hasReviews) {
                System.out.println("No reviews yet.");
            }
            
        } catch (SQLException e) {
            // Handle any SQL exceptions
            System.err.println("Error retrieving reviews: " + e.getMessage());
            e.printStackTrace();
        }
    }
*/

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
