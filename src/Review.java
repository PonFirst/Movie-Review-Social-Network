import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;

public class Review {
    private int reviewID;
    private String text;
    private int rating;
    private int userID;
    private int movieID;
    private Date reviewDate;
    private int likeCount;

    public Review(int reviewID, String text, int rating, int userID, int movieID,
                  Date reviewDate, int likeCount) {
        this.reviewID = reviewID;
        this.text = text;
        this.rating = rating;
        this.userID = userID;
        this.movieID = movieID;
        this.reviewDate = reviewDate;
        this.likeCount = likeCount;
    }

    public void editReview(String text, int rating) {
        this.text = text;
        this.rating = rating;
    }

    public void deleteReview() {
        // TODO: implement delete logic
    }

    public void likeReview() {
        this.likeCount++;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void save() {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Review save failed: database connection is null");
            return;
        }

        String query = "INSERT INTO reviews (movieID, userID, content, rating, reviewDate, likeCount) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.movieID);
            statement.setInt(2, this.userID);
            statement.setString(3, this.text);
            statement.setInt(4, this.rating);
            statement.setDate(5, new java.sql.Date(this.reviewDate.getTime()));
            statement.setInt(6, this.likeCount);

            statement.executeUpdate();
            System.out.println("Review saved to database.");
        } catch (SQLException e) {
            System.err.println("Review save failed: " + e.getMessage());
        }
    }

    public static ArrayList<Review> load() {
        ArrayList<Review> reviews = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();
        String query = "SELECT * FROM Reviews";

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int reviewID = rs.getInt("reviewID");
                int movieID = rs.getInt("movieID");
                int userID = rs.getInt("userID");
                String content = rs.getString("content");
                int rating = rs.getInt("rating");
                Date reviewDate = rs.getDate("reviewDate");
                int likeCount = rs.getInt("likeCount");

                Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error loading reviews: " + e.getMessage());
        }

        return reviews;
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        String formattedDate = dateFormat.format(reviewDate);
        String movieTitle = Movie.getMovieTitleByID(movieID);

        return "Review ID: " + reviewID + "\n" +
                "Movie: " + movieTitle + " (ID: " + movieID + ")\n" +
                "User ID: " + userID + "\n" +
                "Rating: " + rating + "/5\n" +
                "Likes: " + likeCount + "\n" +
                "Date: " + formattedDate + "\n" +
                "Review:\n" + text + "\n" +
                "--------------------";
    }
}