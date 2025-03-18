import javax.swing.*;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<User> users = new ArrayList<>();

        ArrayList<String> genres = new ArrayList<>();
        genres.add("Science Fiction");
        genres.add("Fantasy");
        genres.add("Comedy");
        User user1 = new User("First", "First@gmail.com", "1234", genres);
        users.add(user1);


        MainFrame mainFrame = new MainFrame(users);
        mainFrame.setVisible(true);
    }
}