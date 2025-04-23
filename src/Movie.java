import java.util.ArrayList;
import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Movie
{
    private int movieID;
    private String title;
    private Genre.GenreType genre;


    public Movie(int movieID, String title, Genre.GenreType genre)
    {
        this.movieID = movieID;
        this.title = title;
        this.genre = genre;
    }

    public int getMovieID()
    {
        return movieID;
    }

    public void setMovieID(int movieID)
    {
        this.movieID = movieID;
    }

    public String getMovieTitle()
    {
        return title;
    }

    public void setMovieTitle(String title)
    {
        this.title = title;
    }

    public Genre.GenreType getGenre() {
        return genre;
    }

    public void setGenre(Genre.GenreType genre)
    {
        this.genre = genre;
    }

    public double getAverageRating()
    {
        String sql = "SELECT AVG(rating) AS averageRating FROM reviews WHERE movieID = ?";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, this.movieID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("averageRating");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating: " + e.getMessage());
        }
        return 0.0;
    }

    public static Movie createMovie()
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter new movie title: ");
        String title = scanner.nextLine();

        System.out.print("Enter genre (e.g., ACTION, COMEDY, DRAMA, etc.): ");
        String genreInput = scanner.nextLine().toUpperCase();

        Genre.GenreType genre;
        try {
            genre = Genre.GenreType.valueOf(genreInput);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid genre. Movie not created.");
            return null;
        }

        String sql = "INSERT INTO Movies (title, genres) VALUES (?, ?)";
        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, title);
            stmt.setString(2, genre.name());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int newMovieID = rs.getInt(1);
                    return new Movie(newMovieID, title, genre);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating new movie: " + e.getMessage());
        }

        return null;
    }

    @Override
    public String toString()
    {
        return "Movie ID: " + movieID + ", Title: " + title + ", Genre: " + genre + ", Average Rating: " + getAverageRating();
    }

    public static String getMovieTitleByID(int movieID)
    {
        String title = "Unknown";
        String sql = "SELECT title FROM Movies WHERE id = ?";

        try (Connection conn = Database.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, movieID);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    title = rs.getString("title");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching movie title: " + e.getMessage());
        }

        return title;  // Return the title or "Unknown" if not found
    }

}
