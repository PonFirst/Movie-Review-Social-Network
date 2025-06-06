import java.util.Scanner;

/**
 * MainMenu class acts as a dispatcher for user interactions once authenticated.
 * It displays the main menu which is all available interactions in the program.
 * It calls methods from other classes such as ReviewManager, UserGraphManager,
 * and AuthenticationManager to perform actions like writing reviews, managing follows,
 * searching for reviews, logging in, and logging out.
 * Authors: Phakin Dhamsirimongkol, Pon Yimcharoen
 */
public class MainMenu
{
    private static MainMenu instance; // Singleton instance of MainMenu
    private AuthenticationManager authManager; // AuthManager instance to handle authentication
    private Scanner scanner; // Single Scanner instance for the application

    /**
     * Private constructor to prevent instantiation from outside the class.
     * Initializes the AuthenticationManager instance.
     */
    private MainMenu()
    {
        authManager = AuthenticationManager.getInstance();
        scanner = new Scanner(System.in);
    }

    /**
     * Returns the singleton instance of MainMenu.
     * If the instance doesn't exist, it initializes it.
     *
     * @return the singleton instance of MainMenu
     */
    public static MainMenu getInstance()
    {
        if (instance == null)
        {
            instance = new MainMenu();
        }
        return instance;
    }

    /**
     * Displays the main menu to the user and handles the user input.
     * Delegates actions to the appropriate classes based on the menu choice.
     * Shows different options based on authentication status.
     */
    public void displayMainMenu()
    {
        if (!authManager.isUserLoggedIn())
        {
            displayAuthMenu();
        }
        User currentUser = authManager.getCurrentUser();

        System.out.println();
        System.out.println("1. Write Movie Review");
        System.out.println("2. Edit Review");
        System.out.println("3. Delete Review");
        System.out.println("4. Find Movie Review");
        System.out.println("5. Follow User");
        System.out.println("6. Unfollow User");
        System.out.println("7. Ask for Follow Suggestion");
        System.out.println("8. Logout");
        System.out.println("9. Exit");

        // Get validated user input
        int option = InputValidator.getValidatedInt(scanner, "Enter your choice: ");
        System.out.println();

        switch (option)
        {
            case 1:
                ReviewManager.getInstance().addReviewMenu(currentUser.getUserID(), scanner);
                break;
            case 2:
                ReviewManager.getInstance().editReviewMenu(currentUser.getUserName(), scanner);
                break;
            case 3:
                ReviewManager.getInstance().deleteReviewMenu(currentUser.getUserName(), scanner);
                break;
            case 4:
                searchReviewMenu(scanner);
                break;
            case 5:
                UserGraphManager.getInstance().followUser(scanner);
                break;
            case 6:
                UserGraphManager.getInstance().unfollowUser(scanner);
                break;
            case 7:
                UserGraphManager.getInstance().followRecomendations();
                return;
            case 8:
                authManager.logout();
                break;
            case 9:
                // Synchronize graph data with database before exit
                Graph.getInstance().disconnect();
                try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e)
                {
                    System.out.println("Thread was interrupted: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
                Database.getInstance().disconnect();
                scanner.close();
                System.exit(0);
                break;
            default:
                System.out.println("Invalid option. Try again.");
        }
    }

    /**
     * Displays the authentication menu for login, registration, or exit.
     * Handles login, registration and application exit process.
     */
    public void displayAuthMenu()
    {
        while (true)
        {
            System.out.println("Authentication Menu:");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("3. Exit");

            int option = InputValidator.getValidatedInt(scanner, "Enter your choice: ");

            switch (option)
            {
                case 1:
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    System.out.println();
                    if (authManager.login(email, password))
                    {
                        UserGraphManager.getInstance().displayLatestReviews();
                        return;
                    }
                    else
                    {
                        System.out.println("Login failed. Please try again.\n");
                    }
                    break;
                case 2:
                    authManager.register();
                    break;
                case 3:
                    // Clean disconnect from graph and database
                    Graph.getInstance().disconnect();
                    try
                    {
                        Thread.sleep(2000);
                    } catch (InterruptedException e)
                    {
                        System.out.println("Thread was interrupted: " + e.getMessage());
                        Thread.currentThread().interrupt(); // Restore interrupted status
                    }
                    Database.getInstance().disconnect();
                    scanner.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Displays a sub menu for different ways to search reviews.
     * Delegates to SearchReview methods based on the user's search input.
     *
     * @param scanner the Scanner object used for input
     */
    private void searchReviewMenu(Scanner scanner)
    {
        while (true)
        {
            System.out.println("Search Reviews By:");
            System.out.println("1. Movie Title");
            System.out.println("2. Genre");
            System.out.println("3. Review Date");
            System.out.println("4. User");
            System.out.println("5. Go Back");

            int choice = InputValidator.getValidatedInt(scanner, "Enter your choice: ");
            System.out.println();

            switch (choice)
            {
                case 1:
                    SearchReview.searchByMovieTitle(scanner);
                    break;
                case 2:
                    SearchReview.searchByGenre(scanner);
                    break;
                case 3:
                    SearchReview.searchByDateRange(scanner);
                    break;
                case 4:
                    SearchReview.searchByUsername(scanner);
                    break;
                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}