/**
 * Main class contains the entry point of the application.
 * It initializes the main menu and starts the application loop.
 */
public class Main
{
    /**
     * The main method that serves as the entry point of the application.
     * Creates an instance of MainMenu and enters the main application loop.
     * 
     * @param args command line arguments (not used)
     */
    public static void main(String[] args)
    {
        // Get the singleton instance of MainMenu
        MainMenu mainMenu = MainMenu.getInstance();
        
        // Start the infinite loop for the application
        while(true)
        {
            mainMenu.displayMainMenu();
        }
    }
}