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

    public void addReviewMenu(User user)
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
            Review review = new Review(0, reviewText, rating, user.getUserID(),
                    selectedMovie.getMovieID(), new Date(), 0);
            review.save();
            System.out.println("Review published successfully.");
        } else {
            System.out.println("Review canceled.");
        }
    }



}
