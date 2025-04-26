import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * SearchReview class handles searching and displaying movie reviews based on different criteria.
 * It provides methods to search reviews by movie title, genre, username, and date range.
 * The class utilizes the Singleton pattern and interacts with the database to get reviews.
 */
public class SearchReview
{
    // Singleton instance of SearchReview
    private static SearchReview instance;

    // Private constructor to prevent instantiation
    private SearchReview()
    {
    }

    // Returns the singleton instance of SearchReview
    public static SearchReview getInstance()
    {
        if (instance == null)
        {
            instance = new SearchReview();
        }
        return instance;
    }

    /**
     * Searches for movies whose titles match the given keyword.
     * @param titleKeyword The keyword to match against movie titles
     * @return A list of movies whose titles contain the given keyword
     */
    public static ArrayList<Movie> match(String titleKeyword)
    {
        ArrayList<Movie> matchedMovies = new ArrayList<>();
        // Changed to use Database.executeQuery
        String sql = "SELECT * FROM movies WHERE title LIKE '%" + titleKeyword + "%'";

        try
        {
            ResultSet rs = Database.getInstance().executeQuery(sql);
            while (rs.next())
            {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String genreStr = rs.getString("genres").trim().toUpperCase().replace(' ', '_');
                Genre.GenreType genre = Genre.GenreType.valueOf(genreStr);
                Movie movie = new Movie(id, title, genre);

                matchedMovies.add(movie);
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error finding matching movies: " + e.getMessage());
        }

        return matchedMovies;
    }

    /**
     * Finds all reviews for a movie based on its title.
     * @param movieTitle The title of the movie
     * @return A list of reviews for the movie
     */
    public static ArrayList<Review> findReviewsByMovie(String movieTitle)
    {
        ArrayList<Review> reviews = new ArrayList<>();
        
        // First, find matching movies
        ArrayList<Movie> matchedMovies = match(movieTitle);

        if (matchedMovies.isEmpty()) {
            System.out.println("No movies found with title containing: " + movieTitle);
            return reviews;
        }

        try
        {
            for (Movie movie : matchedMovies)
            {
                int movieId = movie.getMovieID(); // Assuming Movie has a getId() method
                String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                            "FROM reviews r WHERE r.movieID = " + movieId;

                ResultSet rs = Database.getInstance().executeQuery(sql);
                while (rs.next())
                {
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
        }
        catch (SQLException e)
        {
            System.err.println("Error finding reviews by movie: " + e.getMessage());
        }

        return reviews;
    }


    /**
     * Finds all reviews based on username.
     * @param username The username of the reviewer
     * @return A list of reviews written by the user
     */
    public static ArrayList<Review> findReviewsByUsername(String username)
    {
        ArrayList<Review> reviews = new ArrayList<>();
        // Changed to use Database.executeQuery
        String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                "FROM reviews r JOIN users u ON r.userID = u.userID WHERE u.username = '" + username + "'";

        try
        {
            ResultSet rs = Database.getInstance().executeQuery(sql);
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
        catch (SQLException e)
        {
            System.err.println("Error finding reviews by username: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Finds all reviews for movies of a specific genre.
     * @param genreInput The genre to search for
     * @return A list of reviews for movies in the given genre
     */
    public static ArrayList<Review> findReviewsByGenre(String genreInput)
    {
        ArrayList<Review> reviews = new ArrayList<>();

        // Validate genre
        Genre.GenreType genreType;
        try
        {
            genreType = Genre.GenreType.valueOf(genreInput.trim().toUpperCase().replace(' ', '_'));
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Invalid movie genre: " + genreInput);
            return null;
        }

        // Changed to use Database.executeQuery
        String sql = "SELECT r.reviewID, r.content, r.rating, r.userID, r.movieID, r.reviewDate, r.likeCount " +
                "FROM reviews r JOIN movies m ON r.movieID = m.id WHERE m.genres LIKE '%" + genreType.toString() + "%'";

        try
        {
            ResultSet rs = Database.getInstance().executeQuery(sql);
            while (rs.next())
            {
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
        catch (SQLException e)
        {
            System.err.println("Error finding reviews by genre: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Finds all reviews within a given date range.
     * @param startDate The start of the date range
     * @param endDate The end of the date range
     * @return A list of reviews within the date range
     */
    public static ArrayList<Review> findReviewsByDateRange(Date startDate, Date endDate)
    {
        ArrayList<Review> reviews = new ArrayList<>();
        // Changed to use Database.executeQuery
        Timestamp startTimestamp = new Timestamp(startDate.getTime());
        Timestamp endTimestamp = new Timestamp(endDate.getTime() + (24 * 60 * 60 * 1000) - 1);

        String sql = "SELECT * FROM reviews WHERE reviewDate BETWEEN '" + startTimestamp + "' AND '" + endTimestamp + "'";

        try
        {
            ResultSet rs = Database.getInstance().executeQuery(sql);
            while (rs.next())
            {
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
        catch (SQLException e)
        {
            System.err.println("Error finding reviews by date range: " + e.getMessage());
        }

        return reviews;
    }

    /**
     * Asks the user to search for reviews by movie title and displays the results.
     * @param scanner The scanner to read user input
     */
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
                    ReviewManager.getInstance().likeReviewMenu(scanner);
                }
            }

            System.out.print("Do you want to search another movie? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }

    /**
     * Asks the user to search for reviews by genre and displays the results.
     * @param scanner The scanner to read user input
     */
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
                    ReviewManager.getInstance().likeReviewMenu(scanner);
                }
            }

            System.out.print("Do you want to search another genre? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }

    /**
     * Asks the user to search for reviews within a specific date range and displays the results.
     * @param scanner The scanner to read user input
     */
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
                    ReviewManager.getInstance().likeReviewMenu(scanner);
                }
            }

            System.out.print("Do you want to search another date range? (y/n): ");
            String continueSearch = scanner.nextLine().trim();
            if (!continueSearch.equalsIgnoreCase("y")) break;
        }

        System.out.println("Returning to main menu...");
    }

    /**
     * Asks the user to search for reviews by username of the reviewer and displays the results.
     * @param scanner The scanner to read user input
     */
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
                    ReviewManager.getInstance().likeReviewMenu(scanner);
                }
            }

            System.out.print("Do you want to search another user? (y/n): ");
            String again = scanner.nextLine();
            if (!again.equalsIgnoreCase("y")) break;
        }
    }
}