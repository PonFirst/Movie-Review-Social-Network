import java.util.ArrayList;
import java.util.Scanner;

public class MainMenu
{
    private User currentUser;
    private boolean isLoggedIn = false;
    private ArrayList<User> users;
    private Scanner scanner = new Scanner(System.in);

    public MainMenu()
    {
        users = new ArrayList<>();
    }

    public void displayLoginMenu()
    {
        System.out.println("Main Menu");
        while (true)
        {
            System.out.println("1. Register New User");
            System.out.println("2. Login");
            System.out.println("3. Logout");
            System.out.print("Enter your choice: ");

            int option = scanner.nextInt();
            scanner.nextLine();
            switch (option)
            {
                case 1:
                    register();
                    displayMainMenu();
                    break;
                case 2:
                    if (login())
                    {
                        displayMainMenu();
                    }
                    else
                    {
                        displayLoginMenu();
                    }
                    break;
                case 3:
                    System.out.println("3. Logout");
                    System.exit(0);
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    void displayMainMenu()
    {
        // TODO:
        System.out.println("Main Menu");
        while (true)
        {
            System.out.println("1. Write Movie Review");
            System.out.println("2. Edit or Delete Review");
            System.out.println("3. Find Movie Review");
            System.out.println("4. Folow User");
            System.out.println("5. Ask for Follow Suggestion");
            System.out.println("6. Exit");
            System.out.print("Enter your choice: ");

            int option = scanner.nextInt();
            scanner.nextLine();
            switch (option)
            {
                case 1:
                    // TODO:
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
                    System.exit(0);
                default:
                    System.out.println("Invalid option");
                    break;
            }
        }
    }

    public void register()
    {
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Enter favorite genres (Space Separated): ");
        ArrayList<String> favoriteGenres = new ArrayList<>();
        String[] genresArray = scanner.nextLine().split(" ");
        for (String genre : genresArray)
        {
            favoriteGenres.add(genre);
        }

        User newUser = new User(users.size() + 1, username, email,
                password, favoriteGenres, new ArrayList<User>());
        System.out.println("New user created");
        users.add(newUser);
        currentUser = newUser;
        isLoggedIn = true;
    }

    public boolean login()
    {
        System.out.print("Enter email: ");
        String email = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = getUserByEmail(email);
        if (user != null && user.getPassword().equals(password))
        {
            currentUser = user;
            isLoggedIn = true;
            System.out.println("Login successful!");
            return true;
        }
        else
        {
            System.out.println("Invalid email or password.");
            return false;
        }
    }

    private User getUserByEmail(String email)
    {
        for (User user : users)
        {
            if (user.getEmail().equals(email))
            {
                return user;
            }
        }
        return null;
    }

}
