import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.sql.*;

public class Review
{
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

    public int getReviewID()
    {
        return reviewID;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public int getRating()
    {
        return rating;
    }

    public void setRating(int rating)
    {
        this.rating = rating;
    }

    public int getMovieID()
    {
        return movieID;
    }


    public void editReview(String text, int rating)
    {
        this.text = text;
        this.rating = rating;
    }

    public void deleteReview()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Review delete failed: database connection is null");
            return;
        }

        String query = "DELETE FROM reviews WHERE reviewID = ?";

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, this.reviewID);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted > 0) {
                System.out.println("Review successfully deleted from database.");
            } else {
                System.err.println("No review found with the specified ID.");
            }
        } catch (SQLException e) {
            System.err.println("Review delete failed: " + e.getMessage());
        }
    }


    public void likeReview()
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Database connection failed.");
            return;
        }

        // Retrieve the current user through AuthenticationManager
        AuthenticationManager authManager = AuthenticationManager.getInstance();
        int currentUserID = authManager.getCurrentUser().getUserID();

        String checkLikeQuery = "SELECT * FROM Likes WHERE reviewID = ? AND userID = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkLikeQuery)) {
            checkStmt.setInt(1, this.reviewID);
            checkStmt.setInt(2, currentUserID);
            ResultSet resultSet = checkStmt.executeQuery();

            if (resultSet.next()) {
                System.out.println("You have already liked this review.");
                return;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if review is already liked: " + e.getMessage());
            return;
        }

        String insertLikeQuery = "INSERT INTO Likes (reviewID, userID) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertLikeQuery)) {
            insertStmt.setInt(1, this.reviewID);
            insertStmt.setInt(2, currentUserID);
            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                String updateLikeCountQuery = "UPDATE Reviews SET likeCount = likeCount + 1 WHERE reviewID = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateLikeCountQuery)) {
                    updateStmt.setInt(1, this.reviewID);
                    updateStmt.executeUpdate();
                }

                System.out.println("Liked review ID: " + this.reviewID);
            } else {
                System.err.println("Failed to insert like.");
            }
        } catch (SQLException e) {
            System.err.println("Error liking review: " + e.getMessage());
        }
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

    public void update() {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Review update failed: database connection is null");
            return;
        }

        String query = "UPDATE reviews SET content = ?, rating = ? WHERE reviewID = ?";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setString(1, this.text);
            statement.setInt(2, this.rating);
            statement.setInt(3, this.reviewID);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Review updated successfully.");
            } else {
                System.err.println("Review update failed: review ID not found.");
            }
        } catch (SQLException e) {
            System.err.println("Review update failed: " + e.getMessage());
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

    public static Review getReviewByID(int reviewID)
    {
        Connection conn = Database.getInstance().getConnection();
        if (conn == null) {
            System.err.println("Failed to fetch review: database connection is null");
            return null;
        }

        String query = "SELECT * FROM reviews WHERE reviewID = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, reviewID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Review(
                        rs.getInt("reviewID"),
                        rs.getString("content"),
                        rs.getInt("rating"),
                        rs.getInt("userID"),
                        rs.getInt("movieID"),
                        rs.getDate("reviewDate"),
                        rs.getInt("likeCount")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching review: " + e.getMessage());
        }
        return null;
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