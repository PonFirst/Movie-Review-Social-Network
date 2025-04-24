import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.InputMismatchException;
import java.util.Scanner;

public class InputValidator
{
    public static int getValidatedInt(Scanner scanner, String prompt)
    {
        while (true) {
            try {
                System.out.print(prompt);
                int value = scanner.nextInt();
                scanner.nextLine();
                return value;
            } catch (InputMismatchException e)
            {
                System.out.println("Invalid input. Please enter an integer.");
                scanner.nextLine();
            }
        }
    }


    public static boolean isValidEmail(String email)
    {
        return email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }


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
            } catch (ParseException e) {
                System.out.println("Invalid date. Please enter a valid date.");
            }
        }
    }

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
