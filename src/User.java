import java.sql.*;
import java.util.ArrayList;

public class User
{
    private int userID;
    private String username;
    private String email;
    private String password;
    private ArrayList<Genre.GenreType> genres;

    public User(int userID, String username, String email, String password, ArrayList<Genre.GenreType> genres)
    {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.genres = genres != null ? genres : new ArrayList<>();
    }


    public int getUserID()
    {
        return userID;
    }

    public void setUserID(int userID)
    {
        this.userID = userID;
    }

    public String getUserName()
    {
        return username;
    }

    public void setUserName(String username)
    {
        this.username = username;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public ArrayList<Genre.GenreType> getFavoriteGenres()
    {
        return genres;
    }

    public void setFavoriteGenres(ArrayList<Genre.GenreType> genres)
    {
        this.genres = genres;
    }

    public void save() {
        Connection conn = Database.getInstance().getConnection();
        String query = "INSERT INTO users (userID, username, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.userID);
            statement.setString(2, this.username);
            statement.setString(3, this.email);
            statement.setString(4, this.password);
            statement.executeUpdate();

            // Save each genre
            for (Genre.GenreType genre : this.genres) {
                String genreQuery = "INSERT INTO UserGenres (userID, genre) VALUES (?, ?)";
                try (PreparedStatement genreStatement = conn.prepareStatement(genreQuery)) {
                    genreStatement.setInt(1, this.userID);
                    genreStatement.setString(2, genre.name());
                    genreStatement.executeUpdate();
                }
            }
            System.out.println("User and genres saved to database.");
        } catch (SQLException e) {
            System.err.println("User save failed: " + e.getMessage());
        }
    }



    public static ArrayList<User> load()
    {
        ArrayList<User> users = new ArrayList<>();
        Connection connection = Database.getInstance().getConnection();
        String query = "SELECT * FROM users";

        try (PreparedStatement statement = connection.prepareStatement(query))
        {
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next())
            {
                int userID = resultSet.getInt("userID");
                String username = resultSet.getString("username");
                String email = resultSet.getString("email");
                String password = resultSet.getString("password");

                // Load genres for the user
                ArrayList<Genre.GenreType> genres = new ArrayList<>();
                String genreQuery = "SELECT genre FROM UserGenres WHERE userID = ?";
                try (PreparedStatement genreStatement = connection.prepareStatement(genreQuery))
                {
                    genreStatement.setInt(1, userID);
                    ResultSet genreResultSet = genreStatement.executeQuery();
                    while (genreResultSet.next())
                    {
                        try
                        {
                            genres.add(Genre.GenreType.valueOf(genreResultSet.getString("genre")));
                        }
                        catch (IllegalArgumentException e)
                        {
                            System.err.println("Invalid genre in database.");
                        }
                    }
                }

                users.add(new User(userID, username, email, password, genres));
            }
        }
        catch (SQLException exception)
        {
            System.err.println("Failed to load users: " + exception.getMessage());
        }

        return users;
    }


    public static int getNextUserID()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "SELECT MAX(userID) AS max_id FROM users";
        try (PreparedStatement stmt = conn.prepareStatement(query))
        {
            ResultSet result = stmt.executeQuery();
            if (result.next())
            {
                return result.getInt("max_id") + 1;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get max user ID: " + e.getMessage());
        }
        return 1;
    }


}
