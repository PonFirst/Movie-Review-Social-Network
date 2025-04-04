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
            authManager.displayAuthMenu();
        }

        System.out.println("Main Menu");
        while (true)
        {
            System.out.println("1. Write Movie Review");
            System.out.println("2. Edit or Delete Review");
            System.out.println("3. Find Movie Review");
            System.out.println("4. Follow User");
            System.out.println("5. Ask for Follow Suggestion");
            System.out.println("6. Logout");
            System.out.println("7. Exit");
            System.out.print("Enter your choice: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option)
            {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
                case 6:
                    authManager.logout();
                    displayMainMenu();
                    return;
                case 7:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }
}
