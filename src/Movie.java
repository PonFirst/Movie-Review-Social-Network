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
     *
     * @return the average rating, or 0.0 if there are no reviews or an error occurs
     */
    public double getAverageRating()
    {
        String sql = "SELECT AVG(rating) AS averageRating FROM reviews WHERE movieID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql))
        {
            statement.setInt(1, this.movieID);
            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    return resultSet.getDouble("averageRating");
                }
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

        String sql = "INSERT INTO Movies (title, genres) VALUES (?, ?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            statement.setString(1, title);
            statement.setString(2, genre.name());
            statement.executeUpdate();

            try (ResultSet resultSet = statement.getGeneratedKeys())
            {
                if (resultSet.next())
                {
                    int newMovieID = resultSet.getInt(1);
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
     * @param movieID the ID of the movie to look up
     * @return the movie title if found, otherwise "Unknown"
     */
    public static String getMovieTitleByID(int movieID)
    {
        String title = "Unknown";
        String sql = "SELECT title FROM Movies WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement statement = conn.prepareStatement(sql))
        {
            statement.setInt(1, movieID);

            try (ResultSet resultSet = statement.executeQuery())
            {
                if (resultSet.next())
                {
                    title = resultSet.getString("title");
                }
            }
        }
        catch (SQLException e)
        {
            System.err.println("Error fetching movie title: " + e.getMessage());
        }
        return title;
    }
}
