import java.util.ArrayList;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        MainMenu mainMenu = MainMenu.getInstance();
        mainMenu.displayAuthMenu();
        // Load users from database into the graph structure
        Graph.getInstance();
        while(true)
        {
            mainMenu.displayMainMenu();
        }

    }
}