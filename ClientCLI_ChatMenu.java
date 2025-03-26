package cliclient;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientCLI {
    private PrintWriter writer;
    private BufferedReader reader;
    private Socket socket;
    private Scanner scanner;

    public ClientCLI() {
        scanner = new Scanner(System.in);
    }

    public void contactServer() {
        try {
            socket = new Socket("127.0.0.1", 2000);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to server.");
            loginOrSignup();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());
        }
    }

    private void loginOrSignup() {
        System.out.print("Do you want to (1) Log in or (2) Sign up? Enter choice: ");
        int choice = scanner.nextInt();
        scanner.nextLine();

        if (choice == 1) {
            System.out.print("Enter your student ID: ");
            String userId = scanner.nextLine();
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();
            writer.println("LOGIN," + userId + "," + password);
        } else if (choice == 2) {
            System.out.print("Choose a new ID: ");
            String newUserId = scanner.nextLine();
            System.out.print("Choose a new password: ");
            String newPassword = scanner.nextLine();
            writer.println("SIGNUP," + newUserId + "," + newPassword);
            loginOrSignup();
        } else {
            System.out.println("Invalid choice.");
            loginOrSignup();
        }

        receiveResponse();
    }

    private void receiveResponse() {
        try {
            String response = reader.readLine();
            System.out.println("Server Response: " + response);
        } catch (IOException e) {
            System.err.println("Error reading response from server: " + e.getMessage());
        }
    }

    public void sendText() {
        if (writer == null) {
            System.out.println("Not connected to server.");
            return;
        }

        System.out.print("Enter recipient (or type 'everyone' for global message): ");
        String recipient = scanner.nextLine();

        System.out.println("Enter text to send (type 'exit' to stop):");
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            writer.println("MESSAGE," + recipient + "," + line);
        }
    }

    public void requestActiveMembers() {
        if (writer == null) {
            System.out.println("Not connected to server.");
            return;
        }

        writer.println("ACTIVE_MEMBERS");
        System.out.println("Requesting active members...");
    }

    public void requestReadMessages() {
        if (writer == null) {
            System.out.println("Not connected to server.");
            return;
        }

        writer.println("READ_MESSAGES");
        System.out.println("Requesting stored messages...");
    }
    public void requestPrivateReadMessages()
	{
		if(writer == null) {
			System.out.println("Not connected to server.");
			return;
		}
		
		writer.println("READ_PRIVATE_MESSAGES");
		try {
			String response;
			System.out.println("Your Private Messages: ");
			while((response = reader.readLine()) != null) {
				if (response.equals("END_OF_MESSAGES")) break;
				System.out.println(response);
			}
		}catch(IOException e) {
			System.err.println("Error reading private messages.");
		}
		System.out.println("Requesting stored private messages...");
	}

    public void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    if (message.startsWith("ACTIVE_MEMBERS:")) {
                        System.out.println("\n Active Members: " + message.substring(15));
                    } else if (message.startsWith("STORED_MESSAGES:")) {
                        System.out.println("\n Stored Messages:\n" + message.substring(16));
                    } else {
                        System.out.println("\n New Message: " + message);
                    }
                    System.out.print("> ");
                }
            } catch (IOException e) {
                System.err.println("Disconnected from server.");
            }
        }).start();
    }
    

    public void showMenu() {
        while (true) {
            System.out.println("\nCLI Chat Menu");
            System.out.println("1. Send a Message");
            System.out.println("2. View Active Members");
            System.out.println("3. Read Messages");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    sendText();
                    break;
                case 2:
                    requestActiveMembers();
                    break;
                case 3:
                    requestReadMessages();
                    break;
                case 4:
                    System.out.println("Exiting...");
                    closeConnection();
                    return;
                default:
                    System.out.println("Invalid input. Try again.");
            }
        }
    }

    public void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
                System.out.println("Connection closed.");
            }
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ClientCLI client = new ClientCLI();
        client.contactServer();
        client.receiveMessages();
        client.showMenu();
    }
}
