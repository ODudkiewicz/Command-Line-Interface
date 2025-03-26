package clifrontend;

import java.io.*;
import java.net.*;
import java.util.Scanner;


public class ClientCLI {
	private PrintWriter writer;
	private Socket socket;
	private Scanner scanner;
	private BufferedReader reader;
	
	public ClientCLI() {
		scanner = new Scanner(System.in);
	}
	
	public void contactServer() {
		try {
			socket = new Socket("127.0.0.1", 2000);
			OutputStream os = socket.getOutputStream();
			writer = new PrintWriter(os, true);
			InputStream is = socket.getInputStream();
			reader = new BufferedReader(new InputStreamReader(is));
			writer.println(" ");
			
			System.err.println("Connecting to server...");
		} catch (IOException e) {
			System.err.println("Error connecting to server: " + e.getMessage());
		}
	}
	
	private void Login_Signup() {
		System.out.print("Do you want to (1) Log in or (2) Sign up? Enter Choice: ");
		int choice = scanner.nextInt();
		scanner.nextLine();
		
		if(choice == 1) {
			System.out.println("Login Section:");
			System.out.print("Enter your Student ID: ");
			String userID = scanner.nextLine();
			System.out.print("Enter your password");
			String password = scanner.nextLine();
			writer.println("LOGIN," + userID + "," + password);
		}else if (choice == 2) {
			System.out.println("Signup section");
			System.out.print("Enter your student ID: ");
			String signupUserID = scanner.nextLine();
			System.out.print("Enter your password: ");
			String signupPassword = scanner.nextLine();
			writer.println("SIGNUP," + signupUserID + "," + signupPassword);
		}
		else {
			System.out.println("Invalid choice.");
			Login_Signup();
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
		if(writer == null) {
			System.out.println("Not connected to server. ");
			return;
		}
		System.out.println("Do you want to view global or private messages? \n (For global, write Global or 'G'): ");
		String recipient = scanner.nextLine();
		while(true) {
			System.out.println("Enter a text to send a message: \n(type 'exit' to stop):");
			String line = scanner.nextLine();
			if (line.equalsIgnoreCase("exit")) {
				break;
			}
			writer.println("MESSAGE," + recipient + "," + line);
			receiveResponse();
		}
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
			System.out.println("Requesting stored private messages...");
		}

		public void receiveMessages(){
			if (reader == null) {
				System.out.println("Not connected to server.");
				return;
			}
			new Thread(()-> {
				try {
					String message;
					sychronized (reader)
						while ((message = reader.readLine()) != null) {
							if (message.startsWith("ACTIVE_MEMBERS:")){
								System.out.println("Active members: " + message.substring(15));
							} else if (message.startsWith("STORED_MESSAGES:")){
								System.out.println("Stored messages: " + message.substring(16));
							} else if (message.startsWith("PRIVATE_MESSAGES:")){
								System.out.println("Private  messages: " + message.substring(16));
							} else{
								System.out.println("Server: " + message);
							}
							System.out.print("Enter a text to send a message: \n");
							}
						} catch (IOException e) {
							System.err.println("Error reading response from server: " + e.getMessage());
						}
					}
			}).start();			
		}
	public void showMenu(){
		if (writer == null) {
			System.out.println("Not connected to server.");
			return;
		}
		while(true) {
			System.out.println("1. Send a message");
			System.out.println("2. Read messages");
			System.out.println("3. Read private messages");
			System.out.println("4. Show Active Members");
			System.out.println("5. Exit");
			System.out.print("Enter choice: ");
			String input = scanner.nextLine();
			int choice = -1;
			try {
				choice = Integer.parseInt(input);
			} catch (NumberFormatException e) {
				System.out.println("Invalid choice. Try again. ");
				continue;
			}
			}
			scanner.nextLine();
			switch(choice) {
				case 1:
					sendText();
					break;
				case 2:
					requestReadMessages();
					break;
				case 3:
					requestPrivateReadMessages();
					break;
				case 4:
					writer.println("ACTIVE_MEMBERS");
					break;
				case 5:
					writer.println("EXIT");
					closeConnection();
					return;
				default:
					System.out.println("Invalid choice. Try again.");
			}
		}


	public void closeConnection() {
		try {
			if(socket != null) {
				socket.close();
				System.out.println("Connection closed");
			}
			if (scanner != null) {
				scanner.close();
			}
			
		}catch (IOException e) {
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