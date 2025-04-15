import java.sql.*;
import java.util.ArrayList;

public class User
{
    private int userID;
    private String username;
    private String email;
    private String password;


    public User(int userID ,String username, String email, String password)
    {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
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

    public void save()
    {
        Connection conn = Database.getInstance().getConnection();
        String query = "INSERT INTO users (userID, username, email, password) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, this.userID);
            statement.setString(2, this.username);
            statement.setString(3, this.email);
            statement.setString(4, this.password);
            statement.executeUpdate();
            System.out.println("User saved to database.");
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
                users.add(new User(
                        resultSet.getInt("userID"),
                        resultSet.getString("username"),
                        resultSet.getString("email"),
                        resultSet.getString("password")
                ));
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
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
            {
                return rs.getInt("max_id") + 1;
            }
        }
        catch (SQLException e)
        {
            System.err.println("Failed to get max user ID: " + e.getMessage());
        }
        return 1;
    }


}
