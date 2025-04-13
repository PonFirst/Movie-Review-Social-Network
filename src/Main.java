import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        MainMenu mainMenu = MainMenu.getInstance();
        ArrayList<User> users = User.load();
        while(true)
        {
            mainMenu.displayMainMenu();
        }
    }
}