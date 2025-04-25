import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.sql.*;

public class Review
{
    private int reviewID;   // Unique identifier for the review
    private String text;    // Review text
    private int rating;     // Movie rating from 1 to 5
    private int userID;     // The ID of the user who wrote the review
    private int movieID;    // The ID of the movie being reviewed
    private Date reviewDate;    // The date the review was written
    private int likeCount;  // The amount of likes the review has

    /**
     * Constructor to initialize a Review object.
     * @param reviewID   the unique identifier for the review
     * @param text       the content of the review
     * @param rating     the rating given to the movie
     * @param userID     the ID of the user who wrote the review
     * @param movieID    the ID of the movie being reviewed
     * @param reviewDate the date the review was written
     * @param likeCount  the number of likes the review has received
     */
    public Review(int reviewID, String text, int rating, int userID, int movieID,
                  Date reviewDate, int likeCount)
    {
        this.reviewID = reviewID;
        this.text = text;
        this.rating = rating;
        this.userID = userID;
        this.movieID = movieID;
        this.reviewDate = reviewDate;
        this.likeCount = likeCount;
    }

    /**
     * Get the review's unique identifier
     * @return the review ID
     */
    public int getReviewID()
    {
        return reviewID;
    }

    /**
     * Get the content of the review
     * @return the review text
     */
    public String getText()
    {
        return text;
    }

    /**
     * Sets the content of the review
     * @param text the new review text
     */
    public void setText(String text)
    {
        this.text = text;
    }

    /**
     * Get the rating given to the movie
     * @return the review rating
     */
    public int getRating()
    {
        return rating;
    }

    /**
     * Sets the rating given to the movie
     * @param rating the new rating
     */
    public void setRating(int rating)
    {
        this.rating = rating;
    }

    /**
     * Get the ID of the movie being reviewed.
     * @return the movie ID
     */
    public int getMovieID()
    {
        return movieID;
    }

    /**
     * Deletes the review from the database.
     * Uses the reviewID to remove the corresponding record from the reviews table.
     */
    public void deleteReview()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null)
        {
            System.err.println("Review delete failed: database connection is null");
            return;
        }

        String query = "DELETE FROM reviews WHERE reviewID = ?";

        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, this.reviewID);
            int rowsDeleted = statement.executeUpdate();
            if (rowsDeleted > 0)
            {
                System.out.println("Review successfully deleted from database.");
            } else {
                System.err.println("No review found with the specified ID.");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Review delete failed: " + e.getMessage());
        }
    }

    /**
     * Allows the current user to like the review.
     * Checks if the user has already liked the review and, if not, inserts a like record
     * and increments the like count in the database.
     */
    public void likeReview()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null)
        {
            System.err.println("Database connection failed.");
            return;
        }
        // Get the current user from AuthenticationManager
        AuthenticationManager authManager = AuthenticationManager.getInstance();
        int currentUserID = authManager.getCurrentUser().getUserID();

        String checkLikeQuery = "SELECT * FROM Likes WHERE reviewID = ? AND userID = ?";
        try (PreparedStatement statement = conn.prepareStatement(checkLikeQuery))
        {
            statement.setInt(1, this.reviewID);
            statement.setInt(2, currentUserID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                System.out.println("You have already liked this review.");
                return;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error checking if review is already liked: " + e.getMessage());
            return;
        }

        String insertLikeQuery = "INSERT INTO Likes (reviewID, userID) VALUES (?, ?)";
        try (PreparedStatement insertStatement = conn.prepareStatement(insertLikeQuery))
        {
            insertStatement.setInt(1, this.reviewID);
            insertStatement.setInt(2, currentUserID);
            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected > 0)
            {
                String updateLikeCountQuery = "UPDATE Reviews SET likeCount = likeCount + 1 WHERE reviewID = ?";
                try (PreparedStatement updateStatement = conn.prepareStatement(updateLikeCountQuery))
                {
                    updateStatement.setInt(1, this.reviewID);
                    updateStatement.executeUpdate();
                }
                System.out.println("Liked review ID: " + this.reviewID);
            }
            else
            {
                System.err.println("Failed to insert like.");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error liking review: " + e.getMessage());
        }
    }

    /**
     * Checks if a user has already reviewed a specific movie.
     * @param userID  the ID of the user
     * @param movieID the ID of the movie
     * @return true if the user has reviewed the movie, false otherwise
     */
    public static boolean userHasReviewedMovie(int userID, int movieID)
    {
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(
                     "SELECT COUNT(*) FROM Reviews WHERE userID = ? AND movieID = ?"))
        {
            statement.setInt(1, userID);
            statement.setInt(2, movieID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
                return resultSet.getInt(1) > 0;
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Saves the review to the database by inserting a new record
     * @return true if the review was saved successfully, false otherwise
     */
    public boolean save()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null)
        {
            System.err.println("Review save failed: database connection is null");
            return false;
        }

        String query = "INSERT INTO reviews (movieID, userID, content, rating, reviewDate, likeCount) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, this.movieID);
            statement.setInt(2, this.userID);
            statement.setString(3, this.text);
            statement.setInt(4, this.rating);
            statement.setDate(5, new java.sql.Date(this.reviewDate.getTime()));
            statement.setInt(6, this.likeCount);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0)
            {
                System.out.println("Review saved to database.");
                return true;
            }
            else
            {
                System.err.println("Review save failed: no rows affected.");
                return false;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Review save failed: " + e.getMessage());
            return false;
        }
    }


    // Updates the review's text and rating in the database
    public void update()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null)
        {
            System.err.println("Review update failed: database connection is null");
            return;
        }

        String query = "UPDATE reviews SET content = ?, rating = ? WHERE reviewID = ?";

        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setString(1, this.text);
            statement.setInt(2, this.rating);
            statement.setInt(3, this.reviewID);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0)
            {
                System.out.println("Review updated successfully.");
            }
            else
            {
                System.err.println("Review update failed: review ID not found.");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Review update failed: " + e.getMessage());
        }
    }

    /**
     * Loads all reviews from the database and returns them as a list
     * @return a list of all Review objects in the database
     */
    public static ArrayList<Review> load()
    {
        ArrayList<Review> reviews = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();
        String query = "SELECT * FROM Reviews";

        try (PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery())
        {
            while (resultSet.next())
            {
                int reviewID = resultSet.getInt("reviewID");
                int movieID = resultSet.getInt("movieID");
                int userID = resultSet.getInt("userID");
                String content = resultSet.getString("content");
                int rating = resultSet.getInt("rating");
                Date reviewDate = resultSet.getDate("reviewDate");
                int likeCount = resultSet.getInt("likeCount");

                Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                reviews.add(review);
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error loading reviews: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Get a review from the database by its ID.
     * @param reviewID the ID of the review to get
     * @return the Review object if found, or null if not found or an error occurs
     */
    public static Review getReviewByID(int reviewID)
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null)
        {
            System.err.println("Failed to fetch review: database connection is null");
            return null;
        }

        String query = "SELECT * FROM reviews WHERE reviewID = ?";
        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, reviewID);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next())
            {
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
        catch (SQLException e)
        {
            System.err.println("Error fetching review: " + e.getMessage());
        }
        return null;
    }

    /**
     * Provides a string representation of the review, including formatted date and movie title
     * @return a formatted string describing the review
     */
    @Override
    public String toString()
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.ENGLISH);
        dateFormat.setCalendar(new GregorianCalendar());

        String formattedDate = dateFormat.format(reviewDate);
        String movieTitle = Movie.getMovieTitleByID(movieID);

        return "Review ID: " + reviewID + "\n" +
                "Movie: " + movieTitle + " (ID: " + movieID + ")\n" +
                "User ID: " + userID + "\n" +
                "Rating: " + rating + "/5\n" +
                "Likes: " + likeCount + "\n" +
                "Date: " + formattedDate + "\n" +
                "Review: " + text + "\n" +
                "--------------------\n";
    }

}