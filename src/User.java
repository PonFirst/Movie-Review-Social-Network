import java.util.ArrayList;

public class User
{
    private int userID;
    private String username;
    private String email;
    private String password;
    private ArrayList<String> favoriteGenres;
    private ArrayList<User> following;


    public User(int userID ,String username, String email, String password,
                ArrayList<String> favoriteGenres, ArrayList<User> following)
    {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.password = password;
        this.favoriteGenres = favoriteGenres;
        this.following = following;
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

    public ArrayList<String> getFavoriteGenres()
    {
        return favoriteGenres;
    }

    public void setFavoriteGenres(ArrayList<String> favoriteGenres)
    {
        this.favoriteGenres = favoriteGenres;
    }

    public ArrayList<User> getFollowing()
    {
        return following;
    }

}
