package cliclient;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientCLI{
    private PrintWriter p_writer;
    private BufferedReader b_reader;
    private Socket socket;
    private Scanner scanner;
    private String userID;

    public ClientCLI(){
        scanner = new Scanner(System.in);
    }
    public void contactServer(){
        try{
            socket = new Socket("127.0.0.1", 2000);
            p_writer = new PrintWriter(socket.getOutputStream(), true);
            b_reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            System.out.println("Connected to the server");
            loginOrSignup();
        } catch (IOException e) {
            System.err.println("Error connecting to server: " + e.getMessage());

        }
    }

    private void loginOrSignup() {
        System.out.println("Log in / Sign Up Menu: \n");
        System.out.print("Do you want to (1) Log in or (2) Sign up? : ");
        int option = scanner.nextInt();
        scanner.nextLine();
        if(option == 1){
            System.out.print("Enter your student ID here:");
            String userID = scanner.nextLine();
            System.out.print("Enter your password here: ");
            String password = scanner.nextLine();
            p_writer.println("LOGIN, " + userID + "," + password);

        }else if (option == 2){
            System.out.print("Enter your Student ID here: ");
            String newUserID = scanner.nextLine();
            System.out.print("Enter your new password: ");
            String newPassword = scanner.nextLine();
            p_writer.println("SIGNUP, " + newUserID + "," + newPassword);
        }else{
            System.out.println("Invalid option");
            loginOrSignup();
        }

        receiveResponse();
    }
    private void receiveResponse() {
        try{
            String response = b_reader.readLine();
            System.out.println("Server Response: " + response);
        } catch(IOException e) {
            System.err.println("Error reading response from the server... " + e.getMessage());
        }
    }

    public void sendPrivateMessage() {
        if (p_writer == null)
        {
            System.out.println("Not connected to server.");
            return;
        }
        System.out.print("Enter the client's username (or type 'exit' to return to menu): ");
        String recipient = scanner.nextLine();

        if(recipient.equalsIgnoreCase("exit")) {
            System.out.println("Reeturning to menu....");
            return;
        }

        System.out.println("Enter your message (type 'exit' to cancel the process): ");
        while (true) {
            String message = scanner.nextLine();
            if(message.equalsIgnoreCase("exit")){
                System.out.println("Message process cancelled. Returning to menu... ");
                break;
            }
            p_writer.println("[PRIVATE] " + userID + " -> " + recipient + ": " + message);
            System.out.println("Private message sent to: " + recipient + ".");
        }


    }
    public void requestPrivateReadMessages() {
        if (p_writer == null) {
            System.out.println("Not connected to server.");
            return;
        }

        System.out.println("Your Private Messages:\n");
        
        while (true) { 
            String message = scanner.nextLine();
            
            if (message.equalsIgnoreCase("exit")) {
                System.out.println("Returning to main menu...");
                break; 
            }
            
            p_writer.println(message); 
        }
    }
    public void sendText() {
        if(p_writer == null){
            System.out.println("Not connected to server.");
            return;
        }
        System.out.print("Enter recipient (or type 'everyone' for global message): ");
        String recipient = scanner.nextLine();

        System.out.println("Enter text to send (type 'exit' to stop): ");
        while(true) {
            String line = scanner.nextLine();
            if(line.equalsIgnoreCase("exit")){
                break;
            }
            p_writer.println("MESSAGE, " + recipient + "," + line);
        }
    }

    public void requestActiveMembers() {
        if(p_writer == null){
            System.out.println("Not connect to server.");
            return;
        }
        p_writer.println("ACTIVE_MEMBERS");
        System.out.println("Requesting active members...");
    }



    public void requestReadMessages() {
        if (p_writer == null) {
            System.out.println("Not connected to server.");
            return;
        }

        p_writer.println("READ_MESSAGES");
        System.out.println("Requesting stored messages...");
    }

    public void receiveMessages() {
        new Thread(() -> {
            try {
                String message;
                while ((message = b_reader.readLine()) != null) {
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
            System.out.println("4. Private Message");
            System.out.println("5. Exit");
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
                	sendPrivateMessage();
                case 5:
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
    
    public void exit() {
    	
    }

    public static void main(String[] args) {
        ClientCLI client = new ClientCLI();
        client.contactServer();
        client.receiveMessages();
        client.showMenu();
    }
}
