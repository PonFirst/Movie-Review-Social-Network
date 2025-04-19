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

    public static ArrayList<Review> findReviewsByMovie(String movieTitle) {
        ArrayList<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                "FROM reviews r JOIN movies m ON r.movieID = m.id WHERE LOWER(m.title) LIKE LOWER(?)"; // Normalize both sides

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Normalize user input to match with the movie titles in the database
            stmt.setString(1, "%" + movieTitle.trim().toLowerCase() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    String content = rs.getString("content");
                    int rating = rs.getInt("rating");
                    int userID = rs.getInt("userID");
                    int movieID = rs.getInt("movieID");
                    Date reviewDate = rs.getDate("reviewDate");
                    int likeCount = rs.getInt("likeCount");

                    Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding reviews by movie: " + e.getMessage());
        }

        return reviews;
    }

    public static ArrayList<Review> findReviewsByUsername(String username) {
        ArrayList<Review> reviews = new ArrayList<>();

        String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                "FROM reviews r JOIN users u ON r.userID = u.userID WHERE u.username = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    String content = rs.getString("content");
                    int rating = rs.getInt("rating");
                    int userID = rs.getInt("userID");
                    int movieID = rs.getInt("movieID");
                    Date reviewDate = rs.getDate("reviewDate");
                    int likeCount = rs.getInt("likeCount");

                    Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                    reviews.add(review);
                }

            }
        } catch (SQLException e) {
            System.err.println("Error finding reviews by username: " + e.getMessage());
        }

        return reviews;
    }

    public static ArrayList<Review> findReviewsByGenre(String genreInput) {
        ArrayList<Review> reviews = new ArrayList<>();

        // Validate genre
        Genre.GenreType genreType;
        try {
            genreType = Genre.GenreType.valueOf(genreInput.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid movie genre: " + genreInput);
            return null;
        }

        String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                "FROM reviews r JOIN movies m ON r.movieID = m.id WHERE m.genres LIKE ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Use wildcards to allow genre to be part of a list, e.g., 'ADVENTURE, FANTASY'
            stmt.setString(1, "%" + genreType.toString() + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    String content = rs.getString("content");
                    int rating = rs.getInt("rating");
                    int userID = rs.getInt("userID");
                    int movieID = rs.getInt("movieID");
                    Date reviewDate = rs.getDate("reviewDate");
                    int likeCount = rs.getInt("likeCount");

                    Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                    reviews.add(review);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding reviews by genre: " + e.getMessage());
        }

        return reviews;
    }







}
