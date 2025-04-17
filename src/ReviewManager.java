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

    public void addReviewMenu(User user) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter Movie Title (or part of it): ");
        String movieTitle = scanner.nextLine();

        // Use match() to find movies (not reviews)
        ArrayList<Movie> matchedMovies = SearchReview.match(movieTitle);

        if (matchedMovies.isEmpty()) {
            System.out.println("No matching movies found.");
            return;
        }

        System.out.println("Matching Movies:");
        for (Movie m : matchedMovies)
        {
            System.out.println(m);
        }

        Movie movie = matchedMovies.get(0);

        System.out.print("Enter rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine();  // Consume newline

        System.out.println("Write your review:");
        String reviewText = scanner.nextLine();

        System.out.print("Confirm submission? (y/n): ");
        if (scanner.nextLine().equalsIgnoreCase("y")) {
            Review review = new Review(reviewText, rating, user, movie, new Date(), 0, 0);
            review.save();
            System.out.println("Review published successfully.");
        } else {
            System.out.println("Review canceled.");
        }
    }


}
