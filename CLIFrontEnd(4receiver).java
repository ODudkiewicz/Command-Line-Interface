import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class CLIFrontEnd {
    private Scanner scanner;
    private String userId;
    private String userPassword;
    private static final String ACCOUNT_FILE = "storedaccounts.csv";
    private static final String CHAT_FILE = Paths.get(System.getProperty("user.dir"), "chat.csv").toString();
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
            updateLoginStatus(userId, true);
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
                if (credentials.length >= 2 && credentials[0].equals(id) && credentials[1].equals(password)) {
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
            System.out.println("Account created successfully! You may log in.");
            start();
        } else {
            System.out.println("Error: Could not create account.");
        }
    }

    private boolean addAccount(String id, String password) { //AI added
        try (FileWriter writer = new FileWriter(ACCOUNT_FILE, true)) {
            writer.write(id + "," + password + ",false\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to accounts file: " + e.getMessage());
        }
        return false;
    }//no longer AI

    private void updateLoginStatus(String id, boolean status) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials[0].equals(id)) {
                    lines.add(credentials[0] + "," + credentials[1] + "," + status);
                } else {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error updating login status: " + e.getMessage());
        }

        try (FileWriter writer = new FileWriter(ACCOUNT_FILE, false)) {
            for (String l : lines) {
                writer.write(l + "\n");
            }
        } catch (IOException e) {
            System.err.println("Error writing updated accounts: " + e.getMessage());
        }
    }

    public void showMenu() {
        while (true) {
            System.out.println("\nCLI Message Board Menu");
            System.out.println("1. Send a Message");
            System.out.println("2. View Active Members");
            System.out.println("3. Read Messages");
            System.out.println("4. Exit");
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
                    sendMessage(userId);
                    break;
                case 2:
                    viewMembers();
                    break;
                case 3:
                    System.out.println(
                            "What messages do you wish to read? Global or private? \n For Global, write Global or G.");
                    String contact = scanner.nextLine();
                    if (contact.equalsIgnoreCase("global") || contact.equalsIgnoreCase("g")) {
                        contact = "everyone";
                    } else {
                        contact = userId;
                    }
                    viewMessages(contact);
                    break;
                case 4:
                    updateLoginStatus(userId, false);
                    exit();
                    return;
                default:
                    System.out.println("Invalid input. Please enter a valid number: ");
            }
        }
    }

    private boolean sendMessage(String Id) {
        String temp = Id;
        System.out.println(
                "Do you want to view global or private messages? \nType Global or G for global messages, anything else for private messages.");
        String choose = scanner.nextLine();
        if (choose.equalsIgnoreCase("global") || choose.equalsIgnoreCase("g")) {
            temp = "everyone";
        }
        viewMessages(temp);

        System.out.println("Who do you want to send to? \nWrite Everyone or All to send to all.");
        String recipient = scanner.nextLine();
        List<String[]> memberList = AccountsFile(ACCOUNT_FILE);

        do {
            System.out.println("Who do you want to send to? \nWrite Everyone or All to send to all.");
            recipient = scanner.nextLine();
        } while (!recipient.equalsIgnoreCase("everyone") && !recipient.equalsIgnoreCase("all")
                && !SecondColumn(memberList, recipient));
        if (recipient.equalsIgnoreCase("all")) {
            recipient = "everyone";
        }
        System.out.println("Enter Message: ");
        String message = scanner.nextLine();
        System.out.println("(Simulated) Message sent: " + userId + " says: " + message);

        String timeStamp = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        try (FileWriter writer = new FileWriter(CHAT_FILE, true)) {
            writer.write(timeStamp + "," + Id + "," + recipient + "," + message + "\n");
            return true;
        } catch (IOException e) {
            System.err.println("Error sending message to chat" + e.getMessage());
        }
        return false;
    }

    private void viewMembers() {
        System.out.println("Active Members:");
        try (BufferedReader reader = new BufferedReader(new FileReader(ACCOUNT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] credentials = line.split(",");
                if (credentials.length == 3 && Boolean.parseBoolean(credentials[2])) {
                    System.out.println(credentials[0]);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading accounts file: " + e.getMessage());
        }
    }

    private void viewMessages(String receiver) {

        try (BufferedReader reader = new BufferedReader(new FileReader(CHAT_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] msg = line.split(",");
                if (msg.length > 3 && receiver.equalsIgnoreCase(msg[2])) {
                    System.out.println(msg[0] + msg[1] + ":" + msg[3] + "\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading chat file: " + e.getMessage());
        }
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

    public static Boolean SecondColumn(List<String[]> collective, String fwd) {
        for (String[] row : collective) {
            if (row.length > 1 && row[0].equals(fwd)) {
                return true;
            }
        }
        return false;
    }

    public static List<String[]> AccountsFile(String filePath) {
        List<String[]> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line.split(","));
            }
        } catch (IOException e) {
            System.err.println("Error reading accounts file: " + e.getMessage());
        }
        return list;
    }

    public static void main(String[] args) {
        CLIFrontEnd cli = new CLIFrontEnd();
        cli.start();
    }
}
