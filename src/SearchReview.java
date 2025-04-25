import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static ArrayList<Review> findReviewsByDateRange(Date startDate, Date endDate)
    {
        ArrayList<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM reviews WHERE reviewDate BETWEEN ? AND ?";

        Timestamp startTimestamp = new Timestamp(startDate.getTime());
        Timestamp endTimestamp = new Timestamp(endDate.getTime() + (24 * 60 * 60 * 1000) - 1);

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, startTimestamp);
            stmt.setTimestamp(2, endTimestamp);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int reviewID = rs.getInt("reviewID");
                    String content = rs.getString("content");
                    int rating = rs.getInt("rating");
                    int userID = rs.getInt("userID");
                    int movieID = rs.getInt("movieID");
                    Date reviewDate = rs.getTimestamp("reviewDate");
                    int likeCount = rs.getInt("likeCount");

                    Review review = new Review(reviewID, content, rating, userID, movieID, reviewDate, likeCount);
                    reviews.add(review);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error finding reviews by date range: " + e.getMessage());
        }

        return reviews;
    }

    public static void searchByMovieTitle(Scanner scanner)
    {
        while (true)
        {
            System.out.print("Enter Movie Title: ");
            String movieTitle = scanner.nextLine();
            System.out.println();

            ArrayList<Review> reviews = SearchReview.findReviewsByMovie(movieTitle);

            if (reviews.isEmpty())
            {
                System.out.println("No reviews found for movies matching: " + movieTitle);
            }
            else
            {
                System.out.println("Reviews for: " + movieTitle);
                for (Review review : reviews)
                {
                    System.out.println(review);
                }

                boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);

                if (likeChoice)
                {
                    ReviewManager.getInstance().likeReviewMenu();
                }
            }

            System.out.print("Do you want to search another movie? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }

    public static void searchByGenre(Scanner scanner)
    {
        while (true)
        {
            System.out.print("Enter Genre: ");
            String genre = scanner.nextLine();
            ArrayList<Review> reviews = SearchReview.findReviewsByGenre(genre);

            if (reviews == null)
            {
                System.out.print("Do you want to try another genre? (y/n): ");
                String again = scanner.nextLine();
                if (!again.equalsIgnoreCase("y")) break;
                continue;
            }

            if (reviews.isEmpty())
            {
                System.out.println("No reviews found for genre: " + genre);
            }
            else
            {
                System.out.println("Reviews in Genre: " + genre);
                for (Review review : reviews)
                {
                    System.out.println(review);
                }

                boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);

                if (likeChoice)
                {
                    ReviewManager.getInstance().likeReviewMenu();
                }
            }

            System.out.print("Do you want to search another genre? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }

    public static void searchByDateRange(Scanner scanner)
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);

        while (true)
        {
            Date startDate = InputValidator.readValidDate("Enter Start Review Date (YYYY-MM-DD): ", scanner, dateFormat);
            Date endDate = InputValidator.readValidDate("Enter End Review Date (YYYY-MM-DD): ", scanner, dateFormat);

            if (startDate.after(endDate))
            {
                System.out.println("Start date must be before or equal to end date. Try again.");
                continue;
            }

            ArrayList<Review> reviews = SearchReview.findReviewsByDateRange(startDate, endDate);
            String startStr = dateFormat.format(startDate);
            String endStr = dateFormat.format(endDate);

            if (reviews.isEmpty())
            {
                System.out.println("No reviews found between " + startStr + " and " + endStr + ".");
            }
            else
            {
                System.out.println("Reviews from " + startStr + " to " + endStr + ":");
                for (Review review : reviews)
                {
                    System.out.println(review);
                }

                boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);

                if (likeChoice)
                {
                    ReviewManager.getInstance().likeReviewMenu();
                }
            }

            System.out.print("Do you want to search another date range? (y/n): ");
            String continueSearch = scanner.nextLine().trim();
            if (!continueSearch.equalsIgnoreCase("y")) break;
        }

        System.out.println("Returning to main menu...");
    }

    public static void searchByUsername(Scanner scanner)
    {
        while (true)
        {
            System.out.print("Enter Username: \n");
            String username = scanner.nextLine();
            ArrayList<Review> reviews = SearchReview.findReviewsByUsername(username);

            if (reviews.isEmpty())
            {
                System.out.println("No reviews found for user: " + username);
            }
            else
            {
                System.out.println("Reviews by user: " + username);
                for (Review review : reviews)
                {
                    System.out.println(review);
                }

                boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);

                if (likeChoice)
                {
                    ReviewManager.getInstance().likeReviewMenu();
                }
            }

            System.out.print("Do you want to search another user? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }

}
