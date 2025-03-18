import java.util.InputMismatchException;
import java.util.Scanner;

public class CLIFrontEnd {
    private Scanner scanner;
    private String userId;

    public CLIFrontEnd() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the CLI Chat System");
        System.out.print("Enter your ID: ");

        userId = scanner.nextLine();

        showMenu();

    }

    public void showMenu() {
        while (true) {
            System.out.println("\nCLI Message Board Menu");
            System.out.println("1. Send a Message");
            System.out.println("2. View Active Members");
            System.out.println("3. Exit");
            System.out.print("Choose an option:  ");
            int choice = -1;

            try {
                choice = scanner.nextInt();
                scanner.nextLine();

            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a valid number: ");
                scanner.nextLine();
                continue;
            }

            switch (choice) {
                case 1:
                    sendMessage();
                    break;
                case 2:
                    viewMembers();
                    break;
                case 3:
                    exit();
                    return;
                default:
                    System.out.println("Invalid input. Please enter a valid number: ");
            }

        }

    }

    private void sendMessage() {
        System.out.println("Enter Message: ");
        String message = scanner.nextLine();
        System.out.println("(Simulated) Message sent: " + message);
    }

    private void viewMembers() {
        System.out.println("(Stimulated) Active Members: User1, User2, User3");
    }

    private void exit() {
        System.out.print("Are you sure you want to exit? (y/n)");
        String confirmExit = scanner.nextLine();
        if (confirmExit.equalsIgnoreCase("y")) {
            System.out.println("Exiting... Goodbye!");
        } else {
            System.out.println("Resuming...");
        }
    }

    public static void main(String[] args) {
        CLIFrontEnd cli = new CLIFrontEnd();
        cli.start();
    }
}
