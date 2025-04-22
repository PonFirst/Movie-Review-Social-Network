import java.text.ParseException;
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

            int option = InputValidator.getValidatedInt(scanner, "Enter your choice: ");

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

            int option = InputValidator.getValidatedInt(scanner, "Enter your choice: ");

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

            int choice = InputValidator.getValidatedInt(scanner, "Enter your choice: ");

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
                            boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);
                            if (likeChoice)
                            {
                                ReviewManager.getInstance().likeReviewMenu();
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
                            boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);
                            if (likeChoice)
                            {
                                ReviewManager.getInstance().likeReviewMenu();
                            }
                        }

                        System.out.print("Do you want to search another genre? (y/n): ");
                        String again = scanner.nextLine();
                        if (!again.equalsIgnoreCase("y")) break;
                    }
                    break;

                case 3:
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    dateFormat.setLenient(false);

                    while (true) {
                        Date startDate = InputValidator.readValidDate("Enter Start Review Date (YYYY-MM-DD): ", scanner, dateFormat);
                        Date endDate = InputValidator.readValidDate("Enter End Review Date (YYYY-MM-DD): ", scanner, dateFormat);

                        // Validate date range
                        if (startDate.after(endDate)) {
                            System.out.println("Start date must be before or equal to end date. Try again.");
                            continue;
                        }

                        // Search and display reviews
                        ArrayList<Review> reviewsInRange = SearchReview.findReviewsByDateRange(startDate, endDate);
                        String startStr = dateFormat.format(startDate);
                        String endStr = dateFormat.format(endDate);

                        if (reviewsInRange.isEmpty()) {
                            System.out.println("No reviews found between " + startStr + " and " + endStr + ".");
                        } else {
                            System.out.println("Reviews from " + startStr + " to " + endStr + ":");
                            for (Review review : reviewsInRange) {
                                System.out.println(review);
                            }
                            boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);
                            if (likeChoice)
                            {
                                ReviewManager.getInstance().likeReviewMenu();
                            }
                        }

                        // Ask if user wants to continue
                        System.out.print("Do you want to search another date range? (y/n): ");
                        String continueSearch = scanner.nextLine().trim();
                        if (!continueSearch.equalsIgnoreCase("y")) break;
                    }

                    System.out.println("Returning to main menu...");
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
                            boolean likeChoice = InputValidator.confirmYes("Like any review? (y/n): ", scanner);
                            if (likeChoice)
                            {
                                ReviewManager.getInstance().likeReviewMenu();
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
