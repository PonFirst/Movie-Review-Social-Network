import java.sql.*;
import java.util.Scanner;

/**
 * The Movie class represents a movie with attributes such as ID, title, and genre.
 * It provides methods to manage movie data, including creating new movies, retrieving average ratings,
 * and interacting with the database to store and query movie information.
 */
public class Movie
{
    private int movieID;    // Unique identifier for the movie
    private String title;   // Title of the movie
    private Genre.GenreType genre;  // Genre of the movie

    /**
     * Constructs a Movie object with the specified ID, title, and genre.
     *
     * @param movieID The unique ID of the movie.
     * @param title   The title of the movie.
     * @param genre   The genre of the movie.
     */
    public Movie(int movieID, String title, Genre.GenreType genre)
    {
        this.movieID = movieID;
        this.title = title;
        this.genre = genre;
    }

    /**
     * Returns the movie ID
     * @return the movie ID
     */
    public int getMovieID()
    {
        return movieID;
    }

    /**
     * Get the average rating of the movie based on existing reviews in the database.
     * Calculates the average of all ratings for this movie.
     *
     * @return the average rating, or 0.0 if there are no reviews or an error occurs
     */
    public double getAverageRating()
    {
        // Query to calculate average rating for this movie
        String sql = "SELECT AVG(rating) AS averageRating FROM reviews WHERE movieID = " + this.movieID;
        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(sql);
            if (resultSet.next())
            {
                return resultSet.getDouble("averageRating");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Asks the user to enter details for a new movie and saves it to the database.
     * Collects title and genre information, validates it, and persists to the database.
     * 
     * @param scanner Scanner object for user input
     * @return the newly created Movie object, or null if creation fails
     */
    public static Movie createMovie(Scanner scanner)
    {
        System.out.print("Enter new movie title: ");
        String title = scanner.nextLine();

        System.out.print("Enter genre (e.g., ACTION, COMEDY, DRAMA, etc.): ");
        String genreInput = scanner.nextLine().toUpperCase();

        Genre.GenreType genre;
        try
        {
            genre = Genre.GenreType.valueOf(genreInput);
        }
        catch (IllegalArgumentException e)
        {
            System.out.println("Invalid genre. Movie not created.");
            return null;
        }

        // Insert the new movie in the database
        String sql = "INSERT INTO Movies (title, genres) VALUES ('" + title + "', '" + genre.name() + "')";
        try
        {
            int rowsAffected = Database.getInstance().executeUpdate(sql);
            
            if (rowsAffected > 0)
            {
                // Get the ID of the newly inserted movie
                String idQuery = "SELECT last_insert_rowid() as last_id";
                ResultSet resultSet = Database.getInstance().executeQuery(idQuery);
                
                if (resultSet.next())
                {
                    int newMovieID = resultSet.getInt("last_id");
                    return new Movie(newMovieID, title, genre);
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error creating new movie: " + e.getMessage());
        }

        return null;
    }

    /**
     * Returns a string representation of the movie, including ID, title, genre, and average rating.
     * @return a string describing the movie
     */
    @Override
    public String toString()
    {
        return "Movie ID: " + movieID + ", Title: " + title + ", Genre: " + genre + ", Average Rating: " + getAverageRating();
    }

    /**
     * Get the title of a movie from the database using its ID.
     * Static utility method to lookup a movie title.
     * 
     * @param movieID the ID of the movie to look up
     * @return the movie title if found, otherwise "Unknown"
     */
    public static String getMovieTitleByID(int movieID)
    {
        String title = "Unknown";
        // Query to get movie title
        String sql = "SELECT title FROM Movies WHERE id = " + movieID;

        try
        {
            ResultSet resultSet = Database.getInstance().executeQuery(sql);
            if (resultSet.next())
            {
                title = resultSet.getString("title");
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error fetching movie title: " + e.getMessage());
        }
        return title;
    }
}