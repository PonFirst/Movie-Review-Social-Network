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

    @Override
    public String toString()
    {
        return "Movie ID: " + movieID + ", Title: " + title + ", Genre: " + genre;
    }

}
