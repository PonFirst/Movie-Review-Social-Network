import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        MainMenu mainMenu = MainMenu.getInstance();
        mainMenu.displayAuthMenu();
        UserGraphManager.getInstance().displayLatestReviews();
        while(true)
        {
            mainMenu.displayMainMenu();
        }

    }
}