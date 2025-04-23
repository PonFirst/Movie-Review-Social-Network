import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ReviewManager
{
    private static ReviewManager instance;
    private ArrayList<Review> reviews;

    private ReviewManager()
    {
        reviews = new ArrayList<>();
    }

    public static ReviewManager getInstance()
    {
        if (instance == null)
        {
            instance = new ReviewManager();
        }
        return instance;
    }

    public void addReviewMenu(int userID) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Movie Title (or part of it): ");
        String movieTitle = scanner.nextLine();
        ArrayList<Movie> matchedMovies = SearchReview.match(movieTitle);
        Movie selectedMovie = null;

        if (matchedMovies.isEmpty()) {
            System.out.println("No matching movies found.");
            boolean createNew = InputValidator.confirmYes("Would you like to create a new movie? (y/n): ", scanner);
            if (createNew) {
                selectedMovie = Movie.createMovie();
                if (selectedMovie == null) {
                    System.out.println("Failed to create new movie. Review process canceled.");
                    return;
                }
            } else {
                System.out.println("Review process canceled.");
                return;
            }
        } else {
            System.out.println("Matching Movies:");
            for (Movie m : matchedMovies) {
                System.out.println(m.toString());
            }

            if (matchedMovies.size() == 1) {
                selectedMovie = matchedMovies.get(0);
            } else {
                int selectedID = InputValidator.getValidatedInt(scanner, "Enter the Movie ID: ");
                for (Movie m : matchedMovies) {
                    if (m.getMovieID() == selectedID) {
                        selectedMovie = m;
                        break;
                    }
                }

                if (selectedMovie == null) {
                    System.out.println("Invalid Movie ID selected.");
                    return;
                }
            }
        }

        // Check if user already reviewed this movie
        if (Review.userHasReviewedMovie(userID, selectedMovie.getMovieID())) {
            System.out.println("You have already published a review for this movie.");
            return;
        }

        int rating = InputValidator.getValidatedInt(scanner, "Enter rating (1-5): ");
        while (rating < 1 || rating > 5) {
            System.out.println("Invalid rating value. Please enter a rating between 1 and 5.");
            rating = InputValidator.getValidatedInt(scanner, "Enter rating (1-5): ");
        }
        scanner.nextLine(); // Consume newline

        System.out.println("Write your review:");
        String reviewText = scanner.nextLine();

        boolean confirm = InputValidator.confirmYes("Confirm submission? (y/n): ", scanner);
        if (confirm) {
            Review review = new Review(0, reviewText, rating, userID,
                    selectedMovie.getMovieID(), new Date(), 0);
            if (review.save()) {
                System.out.println("Review published successfully.");
            } else {
                System.out.println("Failed to publish review.");
            }
        } else {
            System.out.println("Review canceled.");
        }
    }

    public void editReviewMenu(String username)
    {
        Scanner scanner = new Scanner(System.in);

        // Get reviews by username
        ArrayList<Review> userReviews = SearchReview.findReviewsByUsername(username);

        if (userReviews.isEmpty()) {
            System.out.println("No reviews found for user: " + username);
            return;
        }

        System.out.println("Reviews for " + username + ":");
        for (Review review : userReviews) {
            String textSnippet = review.getText().length() > 50 ? review.getText().substring(0, 50) + "..." : review.getText();

            System.out.println("Review ID: " + review.getReviewID());
            System.out.println("Movie: " + Movie.getMovieTitleByID(review.getMovieID()));
            System.out.println("Rating: " + review.getRating());
            System.out.println("Text: " + textSnippet);
            System.out.println("--------------------");
        }

        System.out.print("Enter the Review ID to edit: ");
        int reviewID = scanner.nextInt();
        scanner.nextLine(); // consume newline

        Review selectedReview = null;
        for (Review review : userReviews) {
            if (review.getReviewID() == reviewID) {
                selectedReview = review;
                break;
            }
        }

        if (selectedReview == null) {
            System.out.println("Invalid Review ID.");
            return;
        }

        System.out.println("Current Review:");
        System.out.println(selectedReview);

        System.out.print("Enter new rating (1-5): ");
        int newRating = scanner.nextInt();
        scanner.nextLine();

        while (newRating < 1 || newRating > 5) {
            System.out.println("Invalid rating value. Please enter a rating between 1 and 5.");
            System.out.print("Enter new rating (1-5): ");
            newRating = scanner.nextInt();
            scanner.nextLine();
        }

        System.out.println("Enter new review text (or leave blank to keep current):");
        String newText = scanner.nextLine();

        selectedReview.setRating(newRating);

        if (!newText.isEmpty())
        {
            selectedReview.setText(newText);
        }


        System.out.print("Confirm changes? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y"))
        {
            selectedReview.update();
        }
        else
        {
            System.out.println("Edit canceled.");
        }
    }

    public void deleteReviewMenu(String username)
    {
        Scanner scanner = new Scanner(System.in);

        ArrayList<Review> userReviews = SearchReview.findReviewsByUsername(username);

        if (userReviews.isEmpty()) {
            System.out.println("You have no reviews to delete.");
            return;
        }

        System.out.println("Your Reviews:");
        for (Review review : userReviews) {
            String textSnippet = review.getText().length() > 50 ? review.getText().substring(0, 50) + "..." : review.getText();

            System.out.println("Review ID: " + review.getReviewID());
            System.out.println("Movie: " + Movie.getMovieTitleByID(review.getMovieID()));
            System.out.println("Rating: " + review.getRating());
            System.out.println("Text: " + textSnippet);
            System.out.println("--------------------");
        }

        System.out.print("Enter the Review ID to delete (or type 0 to cancel): ");
        int reviewID = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (reviewID == 0) {
            System.out.println("Deletion canceled. Returning to main menu.");
            return;
        }

        Review selectedReview = null;
        for (Review review : userReviews) {
            if (review.getReviewID() == reviewID) {
                selectedReview = review;
                break;
            }
        }

        if (selectedReview == null) {
            System.out.println("Invalid Review ID.");
            return;
        }

        System.out.println("Selected Review:");
        System.out.println(selectedReview);

        System.out.print("Are you sure you want to delete this review? (y/n): ");
        String confirm = scanner.nextLine();

        if (confirm.equalsIgnoreCase("y")) {
            selectedReview.deleteReview();
            System.out.println("Review deleted successfully.");
        } else {
            System.out.println("Deletion canceled.");
        }
    }

    public void likeReviewMenu()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Review ID to like: ");
        int reviewID = -1;
        try {
            reviewID = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid review ID format.");
            return;
        }

        Review review = Review.getReviewByID(reviewID);
        if (review == null) {
            System.out.println("Review not found.");
            return;
        }

        review.likeReview();
    }


}
