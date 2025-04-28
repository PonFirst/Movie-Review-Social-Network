import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.sql.*;

/**
 * The Review class represents a movie review with attributes such as content, rating, 
 * and associated user and movie information. It provides methods to manage review data,
 * including saving, updating, deleting, and liking reviews in the database.
 * Authors: Phakin Dhamsirimongkol, Pon Yimcharoen
 */
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
        // SQL query to delete the review
        String query = "DELETE FROM reviews WHERE reviewID = " + this.reviewID;

        try
        {
            int rowsDeleted = Database.getInstance().executeUpdate(query);
            if (rowsDeleted > 0)
            {
                System.out.println("Review successfully deleted from database.");
            } else
            {
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
        // Get the current user from AuthenticationManager
        AuthenticationManager authManager = AuthenticationManager.getInstance();
        int currentUserID = authManager.getCurrentUser().getUserID();

        // Check if the user has already liked this review
        String checkLikeQuery = "SELECT * FROM Likes WHERE reviewID = " + this.reviewID + " AND userID = " + currentUserID;
        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(checkLikeQuery);
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

        // Insert a new like and update the like count
        String insertLikeQuery = "INSERT INTO Likes (reviewID, userID) VALUES (" + this.reviewID + ", " + currentUserID + ")";
        try
        {
            int rowsAffected = Database.getInstance().executeUpdate(insertLikeQuery);
            if (rowsAffected > 0)
            {
                String updateLikeCountQuery = "UPDATE Reviews SET likeCount = likeCount + 1 WHERE reviewID = " + this.reviewID;
                Database.getInstance().executeUpdate(updateLikeCountQuery);
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
     * Static utility method to prevent multiple reviews of the same movie by one user.
     * 
     * @param userID  the ID of the user
     * @param movieID the ID of the movie
     * @return true if the user has reviewed the movie, false otherwise
     */
    public static boolean userHasReviewedMovie(int userID, int movieID)
    {
        // Query to count reviews by this user for this movie
        String query = "SELECT COUNT(*) FROM Reviews WHERE userID = " + userID + " AND movieID = " + movieID;
        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(query);
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
        // Convert Date to timestamp and insert review
        long timestamp = this.reviewDate.getTime();
        String query = "INSERT INTO reviews (movieID, userID, content, rating, reviewDate, likeCount) " +
                "VALUES (" + this.movieID + ", " + this.userID + ", '" + this.text + "', " + 
                this.rating + ", '" + timestamp + "', " + this.likeCount + ")";

        try
        {
            int rowsAffected = Database.getInstance().executeUpdate(query);
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

    /**
     * Updates the review's text and rating in the database
     * Updates existing review content and rating.
     */
    public void update()
    {
        // Query to update review text and rating
        String query = "UPDATE reviews SET content = '" + this.text + "', rating = " + this.rating + 
                " WHERE reviewID = " + this.reviewID;

        try
        {
            int rowsAffected = Database.getInstance().executeUpdate(query);
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
     * Get a review from the database by its ID.
     * Static utility method to retrieve a review by ID.
     * 
     * @param reviewID the ID of the review to get
     * @return the Review object if found, or null if not found or an error occurs
     */
    public static Review getReviewByID(int reviewID)
    {
        // Query to get review by ID
        String query = "SELECT * FROM reviews WHERE reviewID = " + reviewID;
        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(query);
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
        
        // Truncate the review text if it's too long
        String displayText = text;
        int maxLength = 50; // Maximum characters to display
        boolean truncated = false;
        
        if (displayText != null && displayText.length() > maxLength)
        {
            displayText = displayText.substring(0, maxLength - 3) + "...";
            truncated = true;
        }
    
        return "Review ID: " + reviewID + "\n" +
                "Movie: " + movieTitle + " (ID: " + movieID + ")\n" +
                "User ID: " + userID + "\n" +
                "Rating: " + rating + "/5\n" +
                "Likes: " + likeCount + "\n" +
                "Date: " + formattedDate + "\n" +
                "Review: " + displayText + (truncated ? " (truncated)" : "") + "\n" +
                "--------------------\n";
    }
}