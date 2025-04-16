import java.sql.*;
import java.util.ArrayList;

public class SearchReview {
    private static SearchReview instance;

    private SearchReview() {
    }

    public static SearchReview getInstance() {
        if (instance == null) {
            instance = new SearchReview();
        }
        return instance;
    }

    public static ArrayList<Movie> match(String titleKeyword) {
        ArrayList<Movie> matchedMovies = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE title LIKE ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + titleKeyword + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String title = rs.getString("title");
                    String genreStr = rs.getString("genres").trim().toUpperCase().replace(' ', '_');
                    Genre.GenreType genre = Genre.GenreType.valueOf(genreStr);
                    Movie movie = new Movie(id, title, genre);

                    matchedMovies.add(movie);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding matching movies: " + e.getMessage());
        }

        return matchedMovies;
    }

    public ArrayList<Review> findReviewsByMovie(String movieTitle) {
        ArrayList<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.reviewID, r.content, r.rating, r.reviewDate, r.likeCount, m.title " +
                "FROM reviews r JOIN movies m ON r.movieID = m.id WHERE m.title LIKE ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Use prepared statement to avoid SQL injection
            stmt.setString(1, "%" + movieTitle + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                boolean foundReviews = false;
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    String content = rs.getString("content");  // Updated to match the column name
                    int rating = rs.getInt("rating");
                    Date reviewDate = rs.getDate("reviewDate");
                    int likeCount = rs.getInt("likeCount");
                    String movie = rs.getString("title");

                    // Create a Review object and add it to the list
                    Review review = new Review(content, rating, null, null, reviewDate, reviewID, likeCount); // Pass null for user and movie for now
                    reviews.add(review);

                    foundReviews = true;
                }
                if (!foundReviews) {
                    System.out.println("No reviews found for the movie titled: " + movieTitle);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding reviews by movie: " + e.getMessage());
        }

        return reviews; // Return the list of reviews
    }


}
