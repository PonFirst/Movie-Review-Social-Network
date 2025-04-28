import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

/**
 * InputValidator class provides utility methods to validate user input.
 * It handles various input types such as integers, emails, dates, and boolean confirmation.
 * Authors: Phakin Dhamsirimongkol, Pon Yimcharoen
 */
public class InputValidator
{
    /**
     * Validates and returns an integer input from the user.
     * Continuously asks the user for input until a valid integer is provided.
     *
     * @param scanner the Scanner object used for user input
     * @param prompt the message to display to the user
     * @return a valid integer input from the user
     */
    public static int getValidatedInt(Scanner scanner, String prompt)
    {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try
            {
                return Integer.parseInt(input);
            }
            catch (NumberFormatException e)
            {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    /**
     * Validates whether the given email string matches the standard email format.
     * Uses regex pattern to validate email structure.
     *
     * @param email the email string to validate
     * @return true if the email matches the regex pattern, false otherwise
     */
    public static boolean isValidEmail(String email)
    {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    /**
     * Reads and checks a date input from the user to make sure it matches the expected format.
     * Keeps asking the user until a valid date in the correct format is entered.
     *
     * @param prompt the message shown to the user
     * @param scanner the Scanner object for reading user input
     * @param dateFormat the SimpleDateFormat object that defines the correct date format
     * @return a valid Date object based on the user's input
     */
    public static Date readValidDate(String prompt, Scanner scanner, SimpleDateFormat dateFormat)
    {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (!input.matches("\\d{4}-\\d{2}-\\d{2}"))
            {
                System.out.println("Invalid format. Please enter the date in YYYY-MM-DD format.");
                continue;
            }
            try
            {
                return dateFormat.parse(input);
            }
            catch (ParseException e)
            {
                System.out.println("Invalid date. Please enter a valid date.");
            }
        }
    }

    /**
     * Asks the user to confirm an action by entering 'y' for yes or 'n' for no.
     * Keeps asking until a valid response is provided.
     *
     * @param prompt the message shown to the user asking for confirmation
     * @param scanner the Scanner object used to read the user's input
     * @return true if the user enters 'y', false if the user enters 'n'
     */
    public static boolean confirmYes(String prompt, Scanner scanner)
    {
        while (true)
        {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            System.out.println("Invalid input. Please enter 'y' or 'n'.");
        }
    }
}