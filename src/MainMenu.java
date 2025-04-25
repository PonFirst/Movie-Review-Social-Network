import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class MainMenu
{
    private static MainMenu instance;
    private AuthenticationManager authManager;

    private MainMenu()
    {
        authManager = AuthenticationManager.getInstance();
    }

    public static MainMenu getInstance()
    {
        if (instance == null)
        {
            instance = new MainMenu();
        }
        return instance;
    }


    public void displayMainMenu()
    {
        Scanner scanner = new Scanner(System.in);

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

        int option = InputValidator.getValidatedInt(scanner, "Enter your choice: ");
        System.out.println();

        switch (option)
        {
            case 1:
                ReviewManager.getInstance().addReviewMenu(currentUser.getUserID());
                break;
            case 2:
                ReviewManager.getInstance().editReviewMenu(currentUser.getUserName());
                break;
            case 3:
                ReviewManager.getInstance().deleteReviewMenu(currentUser.getUserName());
                break;
            case 4:
                searchReviewMenu(scanner);
                break;
            case 5:
                UserGraphManager.getInstance().followUser();
                break;
            case 6:
                UserGraphManager.getInstance().unfollowUser();
                break;
            case 7:
                UserGraphManager.getInstance().followRecomendations();
                return;
            case 8:
                authManager.logout();
                break;
            case 9:
                Database.getInstance().disconnect();
                Graph.getInstance().disconnect();;
                System.exit(0);
                break;
            case 10:
                Graph graph = Graph.getInstance();
                System.out.println(graph);
                break;
            default:
                System.out.println("Invalid option. Try again.");
        }
    }

    public void displayAuthMenu()
    {
        Scanner scanner = new Scanner(System.in);
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
                    Database.getInstance().disconnect();
                    Graph.getInstance().disconnect();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }
    }

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
