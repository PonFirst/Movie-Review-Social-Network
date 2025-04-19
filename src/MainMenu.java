import java.util.ArrayList;
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

        System.out.println("Main Menu");
        while (true)
        {
            System.out.println("1. Write Movie Review");
            System.out.println("2. Edit Review");
            System.out.println("3. Delete Review");
            System.out.println("4. Find Movie Review");
            System.out.println("5. Follow User");
            System.out.println("6. Ask for Follow Suggestion");
            System.out.println("7. Logout");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            int option = scanner.nextInt();
            scanner.nextLine();

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
                    break;
                case 6:
                    break;
                case 7:
                    authManager.logout();
                    return;
                case 8:
                    Database.getInstance().disconnect();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
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
            System.out.print("Enter your choice: ");

            int option = scanner.nextInt();
            scanner.nextLine();

            switch (option)
            {
                case 1:
                    System.out.print("Enter email: ");
                    String email = scanner.nextLine();
                    System.out.print("Enter password: ");
                    String password = scanner.nextLine();
                    if (authManager.login(email, password))
                    {
                        return;
                    }
                    else 
                    {
                        System.out.println("Login failed. Please try again.");
                    }
                    break;
                case 2:
                    authManager.register();
                    break;
                case 3:
                    Database.getInstance().disconnect();
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
            System.out.print("Enter your choice: ");

            int choice = -1;
            try
            {
                choice = Integer.parseInt(scanner.nextLine()); // Parse the user's input
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue; // Skip to the next iteration of the loop
            }

            switch (choice)
            {
                case 1:
                    while (true) {
                        System.out.print("Enter Movie Title: ");
                        String movieTitle = scanner.nextLine();

                        ArrayList<Review> reviews = SearchReview.findReviewsByMovie(movieTitle);

                        if (reviews.isEmpty()) {
                            System.out.println("No reviews found for movies matching: " + movieTitle);
                        } else {
                            System.out.println("Reviews for: " + movieTitle);
                            for (Review review : reviews) {
                                System.out.println(review);
                            }
                        }

                        System.out.print("Do you want to search another movie? (y/n): ");
                        String again = scanner.nextLine();
                        if (!again.equalsIgnoreCase("y")) break;
                    }
                    break;
                case 2:
                    while (true) {
                        System.out.print("Enter Genre: ");
                        String genre = scanner.nextLine();

                        ArrayList<Review> reviews = SearchReview.findReviewsByGenre(genre);

                        if (reviews == null)
                        {
                            System.out.print("Do you want to try another genre? (y/n): ");
                            String again = scanner.nextLine();
                            if (!again.equalsIgnoreCase("y")) break;
                            continue;
                        }

                        if (reviews.isEmpty())
                        {
                            System.out.println("No reviews found for genre: " + genre);
                        } else {
                            System.out.println("Reviews in Genre: " + genre);
                            for (Review review : reviews) {
                                System.out.println(review);
                            }
                        }

                        System.out.print("Do you want to search another genre? (y/n): ");
                        String again = scanner.nextLine();
                        if (!again.equalsIgnoreCase("y")) break;
                    }
                    break;

                case 3:
                    System.out.print("Enter Review Date (YYYY-MM-DD): ");
                    String date = scanner.nextLine();
                    // To be implemented later
                    break;
                case 4:
                    while (true) {
                        System.out.print("Enter Username: ");
                        String username = scanner.nextLine();

                        ArrayList<Review> reviews = SearchReview.findReviewsByUsername(username);
                        if (reviews.isEmpty()) {
                            System.out.println("No reviews found for user: " + username);
                        } else {
                            System.out.println("Reviews by user: " + username);
                            for (Review review : reviews) {
                                System.out.println(review);
                            }
                        }

                        System.out.print("Do you want to search another user? (y/n): ");
                        String again = scanner.nextLine();
                        if (!again.equalsIgnoreCase("y")) break;
                    }
                    break;

                case 5:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }



}
