import java.util.ArrayList;

public class User
{
    private String username;
    private String email;
    private String password;
    private ArrayList<String> favoriteGenres;


    public User(String username, String email, String password, ArrayList<String> favoriteGenres)
    {
        this.username = username;
        this.email = email;
        this.password = password;
        this.favoriteGenres = favoriteGenres;
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
}
