import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.InputMismatchException;
import java.util.Scanner;

public class CLIFrontEnd {
    private Scanner scanner;
    private String userId;
    private String userPassword;
    private static final String ACCOUNT_FILE = "storedaccounts.csv";
    Path p = Paths.get("");
    String s = System.lineSeparator() + "-";

    public CLIFrontEnd() {
        scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("Welcome to the CLI Chat System");
        System.out.print("Do you want to (1) Log in or (2) Sign up? Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            login();
        } else if (choice == 2) {
            signUp();
        } else {
            System.out.println("Invalid choice. Restarting...");
            start();
        }
    }

    private void login() {
        System.out.print("Enter your ID: ");
        userId = scanner.nextLine();
        System.out.print("Enter your password: ");
        userPassword = scanner.nextLine();

        if (authenticate(userId, userPassword)) {
            System.out.println("Login successful!");
            showMenu();
        } else {
            System.out.println("Invalid credentials. Try again.");
            start();
        }
    }

    private boolean authenticate(String id, String password) {
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 2 && credentials[0].equals(id) && credentials[1].equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading accounts file: " + e.getMessage());
        }
        return false;
    }

    private void signUp() {
        System.out.print("Choose a new ID: ");
        userId = scanner.nextLine();
        System.out.print("Choose a new password: ");
        userPassword = scanner.nextLine();

        if (addAccount(userId, userPassword)) {
            System.out.println("Account created successfully! You can now log in.");
            start();
        } else {
            System.out.println("Error: Could not create account.");
        }
    }

    private boolean addAccount(String id, String password) {
        try (FileWriter writer = new FileWriter(ACCOUNT_FILE, true)) {
            writer.write(id + "," + password + "\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to accounts file: " + e.getMessage());
        }
        return false;
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
        System.out.println("(Simulated) Message sent: " + userId + " says: " + message);

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        String s = System.lineSeparator() + "-" + timeStamp + ":" + userId + " says: " + message;
        Path p = Paths.get("C:\\Users\\xgami\\Documents\\Coursework lol\\AdvPro\\chat.txt");

        try {
            Files.write(p, s.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("An error occurred while writing the message: " + e);
        }
    }

    private void viewMembers() {
        System.out.println("(Simulated) Active Members: User1, User2, User3");
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
