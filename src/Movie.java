import java.util.ArrayList;
import java.sql.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Movie
{
    private int movieID;
    private String title;
    private String genre;

    public Movie(int movieID, String title, String genre)
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

    public String getGenre()
    {
        return genre;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

}
