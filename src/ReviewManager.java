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

    public void addReviewMenu(int userID)
    {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Movie Title (or part of it): ");
        String movieTitle = scanner.nextLine();

        ArrayList<Movie> matchedMovies = SearchReview.match(movieTitle);

        if (matchedMovies.isEmpty()) {
            System.out.println("No matching movies found.");
            return;
        }

        System.out.println("Matching Movies:");
        for (Movie m : matchedMovies) {
            System.out.println("Movie ID: " + m.getMovieID() + ", Title: " + m.getMovieTitle() + ", Genre: " + m.getGenre());
        }

        Movie selectedMovie = null;

        if (matchedMovies.size() == 1) {
            selectedMovie = matchedMovies.get(0);
        } else {
            System.out.print("Enter the Movie ID: ");
            int selectedID = scanner.nextInt();
            scanner.nextLine();  // Consume newline

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

        System.out.print("Enter rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine();

        System.out.println("Write your review:");
        String reviewText = scanner.nextLine();

        System.out.print("Confirm submission? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            Review review = new Review(0, reviewText, rating, userID,
                    selectedMovie.getMovieID(), new Date(), 0);
            review.save();
            System.out.println("Review published successfully.");
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

}
