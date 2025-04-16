import java.util.Date;
import java.sql.*;

public class Review
{
    private String text;
    private int rating;
    private User user;
    private Movie movie;
    private Date reviewDate;
    private int reviewID;
    private int likeCount;

    public Review(String text, int rating, User user, Movie movie, Date reviewDate,
                  int reviewID, int likeCount)
    {
        this.text = text;
        this.rating = rating;
        this.user = user;
        this.movie = movie;
        this.reviewDate = reviewDate;
        this.reviewID = reviewID;
        this.likeCount = likeCount;
    }

    public void editReview(String text, int rating)
    {
        this.text = text;
        this.rating = rating;
    }

    public void deleteReview()
    {
        //TODO
    }

    public void likeReview()
    {
        this.likeCount++;
    }

    public int getLikeCount()
    {
        return likeCount;
    }

    public Movie getMovie()
    {
        return movie;
    }

    public void save() {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Review save failed: database connection is null");
            return;
        }

        String query = "INSERT INTO reviews (movieID, userID, content, rating, reviewDate, likeCount) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query))
        {
            statement.setInt(1, this.movie.getMovieID());
            statement.setInt(2, this.user.getUserID());
            statement.setString(3, this.text);
            statement.setInt(4, this.rating);
            statement.setDate(5, new java.sql.Date(this.reviewDate.getTime()));
            statement.setInt(6, this.likeCount);

            // Execute the update
            statement.executeUpdate();
            System.out.println("Review saved to database.");
        } catch (SQLException e) {
            System.err.println("Review save failed: " + e.getMessage());
        }
    }

}
