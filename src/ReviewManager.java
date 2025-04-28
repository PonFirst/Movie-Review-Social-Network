import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

/**
 * ReviewManager class handles the management of movie reviews, including adding, editing, deleting,
 * and liking reviews. It provides functionality for users to interact with their movie reviews.
 * The class uses singleton design pattern to ensure a single instance is used throughout the
 * application.
 * Authors: Phakin Dhamsirimongkol, Pon Yimcharoen
 */
public class ReviewManager
{
    private static ReviewManager instance;  // Singleton instance of ReviewManager

    /**
     * Private constructor
     */
    private ReviewManager()
    {
    }

    /**
     * Returns the single instance of ReviewManager. If the instance does not exist,
     * it creates a new one.
     * @return The single instance of ReviewManager.
     */
    public static ReviewManager getInstance()
    {
        if (instance == null)
        {
            instance = new ReviewManager();
        }
        return instance;
    }

    /**
     * Allows the user to select a movie either by searching for an existing movie or creating a new one.
     * @param scanner The Scanner object used for input
     * @return The selected Movie object, or null if the process was canceled
     */
    private Movie selectMovie(Scanner scanner)
    {
        System.out.print("Enter Movie Title (or part of it): ");
        String movieTitle = scanner.nextLine();
        ArrayList<Movie> matchedMovies = SearchReview.match(movieTitle);
        Movie selectedMovie = null;

        // If no matching movies, offer to create a new one
        if (matchedMovies.isEmpty())
        {
            System.out.println("No matching movies found.");
            boolean createNew = InputValidator.confirmYes("Would you like to create a new movie? (y/n): ", scanner);
            if (createNew)
            {
                selectedMovie = Movie.createMovie(scanner);
                if (selectedMovie == null)
                {
                    System.out.println("Failed to create new movie. Review process canceled.");
                    return null;
                }
            }
            else
            {
                System.out.println("Review process canceled.");
                return null;
            }
        }
        else
        {
            // Display matched movies and allow selection
            System.out.println("Matching Movies:");
            for (Movie m : matchedMovies)
            {
                System.out.println(m.toString());
            }

            // If only one match, select it automatically
            if (matchedMovies.size() == 1)
            {
                selectedMovie = matchedMovies.get(0);
            }
            else
            {
                // Otherwise, prompt user to select a movie by ID
                int selectedID = InputValidator.getValidatedInt(scanner, "Enter the Movie ID (0 to cancel): ");
                if (selectedID == 0)
                {
                    System.out.println("Review process canceled.");
                    return null;
                }
                for (Movie m : matchedMovies)
                {
                    if (m.getMovieID() == selectedID)
                    {
                        selectedMovie = m;
                        break;
                    }
                }
                if (selectedMovie == null)
                {
                    System.out.println("Invalid Movie ID selected.");
                    return null;
                }
            }
        }
        return selectedMovie;
    }

    /**
     * Get a specific review from a user's review list. It also
     * displays the user's reviews and prompts for a review ID to select.
     *
     * @param username the username of the user whose reviews are to be searched
     * @param scanner the Scanner object used for user input
     * @param action the action to be performed on the review
     * @return the selected Review object, or null if no review is selected or the process is canceled
     */
    private Review selectReview(String username, Scanner scanner, String action)
    {
        ArrayList<Review> userReviews = SearchReview.findReviewsByUsername(username);

        if (userReviews.isEmpty())
        {
            System.out.println("No reviews found for user: " + username);
            return null;
        }

        displayReviewList(username, userReviews);
        int reviewID = InputValidator.getValidatedInt(scanner, "Enter the Review ID to " + action + " (or type 0 to cancel): ");

        if (reviewID == 0)
        {
            System.out.println(Character.toUpperCase(action.charAt(0)) + action.substring(1) + " canceled.");
            return null;
        }

        // Find the selected review by ID
        for (Review review : userReviews)
        {
            if (review.getReviewID() == reviewID)
            {
                return review;
            }
        }
        System.out.println("Invalid Review ID.");
        return null;
    }

    /**
     * Handles the process of adding a new review for a selected movie.
     * @param userID The ID of the user adding the review
     * @param scanner the Scanner object used for user input
     */
    public void addReviewMenu(int userID, Scanner scanner)
    {
        Movie selectedMovie = selectMovie(scanner);
        if (selectedMovie == null)
        {
            return;
        }

        // Check if user has already reviewed this movie
        if (Review.userHasReviewedMovie(userID, selectedMovie.getMovieID()))
        {
            System.out.println("You have already published a review for this movie.");
            return;
        }

        // Get review details from the user
        int rating = InputValidator.getValidatedInt(scanner, "Enter rating (1-5): ");
        while (rating < 1 || rating > 5)
        {
            System.out.println("Invalid rating value. Please enter a rating between 1 and 5.");
            rating = InputValidator.getValidatedInt(scanner, "Enter rating (1-5): ");
        }

        System.out.println("Write your review:");
        String reviewText = scanner.nextLine();

        boolean confirm = InputValidator.confirmYes("Confirm submission? (y/n): ", scanner);
        if (confirm)
        {
            Review review = new Review(0, reviewText, rating, userID,
                    selectedMovie.getMovieID(), new Date(), 0);
            if (review.save())
            {
                System.out.println("Review published successfully.");
            }
            else
            {
                System.out.println("Failed to publish review.");
            }
        }
        else
        {
            System.out.println("Review canceled.");
        }
    }

    /**
     * Allows the user to edit an existing review by selecting a review and updating its rating or text.
     * It also asks the user to confirm changes before saving.
     * @param username the username of the user whose review is to be edited
     * @param scanner the Scanner object used for user input
     */
    public void editReviewMenu(String username, Scanner scanner)
    {
        Review selectedReview = selectReview(username, scanner, "edit");

        if (selectedReview == null)
        {
            return;
        }

        System.out.println("Current Review:");
        System.out.println(selectedReview);

        // Get new review information
        int newRating = InputValidator.getValidatedInt(scanner, "Enter new rating (1-5): ");
        while (newRating < 1 || newRating > 5)
        {
            System.out.println("Invalid rating value. Please enter a rating between 1 and 5.");
            newRating = InputValidator.getValidatedInt(scanner, "Enter new rating (1-5): ");
        }

        System.out.println("Enter new review text (or leave blank to keep current):");
        String newText = scanner.nextLine();

        selectedReview.setRating(newRating);
        if (!newText.isEmpty())
        {
            selectedReview.setText(newText);
        }

        boolean confirm = InputValidator.confirmYes("Confirm changes? (y/n): ", scanner);
        if (confirm)
        {
            selectedReview.update();
        }
        else
        {
            System.out.println("Edit canceled.");
        }
    }

    /**
     * Allows the user to delete an existing review by selecting a
     * review and confirming deletion.
     * @param username the username of the user whose review is to be deleted
     * @param scanner the Scanner object used for user input
     */
    public void deleteReviewMenu(String username, Scanner scanner)
    {
        Review selectedReview = selectReview(username, scanner, "delete");

        if (selectedReview == null)
        {
            return;
        }

        System.out.println("Selected Review:");
        System.out.println(selectedReview);

        boolean confirm = InputValidator.confirmYes("Are you sure you want to delete this review? (y/n): ", scanner);
        if (confirm)
        {
            selectedReview.deleteReview();
            System.out.println("Review deleted successfully.");
        }
        else
        {
            System.out.println("Deletion canceled.");
        }
    }

    /**
     * Allows the user to like a review by entering its ID.
     * Validates the review ID and increments the review's like count if valid.
     * @param scanner the Scanner object used for user input
     */
    public void likeReviewMenu(Scanner scanner)
    {
        int reviewID = InputValidator.getValidatedInt(scanner, "Enter Review ID to like: ");

        Review review = Review.getReviewByID(reviewID);
        if (review == null)
        {
            System.out.println("Review not found.");
            return;
        }

        review.likeReview();
    }

    /**
     * Displays a list of reviews for a given user, showing a summary of each review.
     * Includes review ID, movie title, rating, and a text snippet.
     * @param username the name of the user whose reviews will be shown
     * @param userReviews the list of reviews to display
     */
    void displayReviewList(String username, ArrayList<Review> userReviews)
    {
        System.out.println("Reviews for " + username + ":");
        for (Review review : userReviews)
        {
            String textSnippet = review.getText().length() > 50 ? review.getText().substring(0, 50) + "..." : review.getText();

            System.out.println("Review ID: " + review.getReviewID());
            System.out.println("Movie: " + Movie.getMovieTitleByID(review.getMovieID()));
            System.out.println("Rating: " + review.getRating());
            System.out.println("Text: " + textSnippet);
            System.out.println("--------------------");
        }
    }
}