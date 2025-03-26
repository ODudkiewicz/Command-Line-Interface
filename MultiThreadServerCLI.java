package cliclient;

// not completed yet - just posting it to save later

import java.io.*;
import java.net.*;
import java.util.*;

public class MultiThreadServerCLI {
    private static final int PORT = 2000;
    private static Map<String, PrintWriter> activeUsers = new HashMap<>();
    private static final String CHAT_HISTORY_FILE = "chat.csv";

    public static void main(String[] args) {
        System.out.println("Server running on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String userId;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);

                userId = authenticateUser();
                if (userId == null) {
                    socket.close();
                    return;
                }

                activeUsers.put(userId, writer);
                System.out.println(userId + " connected.");

                String input;
                while ((input = reader.readLine()) != null) {
                    processClientRequest(input);
                }
            } catch (IOException e) {
                System.err.println(userId + " disconnected.");
            } finally {
                if (userId != null) {
                    activeUsers.remove(userId);
                }
                try {
                    socket.close();
                } catch (IOException e) {
                    System.err.println("Error closing socket.");
                }
            }
        }

        private String authenticateUser() throws IOException {
            String credentials = reader.readLine();
            if (credentials == null) return null;

            String[] parts = credentials.split(",");
            if (parts.length < 3) return null;

            String action = parts[0];
            String userId = parts[1];
            String password = parts[2];

            if (action.equals("LOGIN")) {
                if (validateCredentials(userId, password)) {
                    writer.println("LOGIN_SUCCESS");
                    return userId;
                } else {
                    writer.println("LOGIN_FAILED");
                    return null;
                }
            } else if (action.equals("SIGNUP")) {
                saveUser(userId, password);
                writer.println("SIGNUP_SUCCESS");
                return userId;
            }
            return null;
        }

        private boolean validateCredentials(String userId, String password) {
            File file = new File("accounts.txt");
            try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts[0].equals(userId) && parts[1].equals(password)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error reading accounts.");
            }
            return false;
        }

        private void saveUser(String userId, String password) {
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter("accounts.txt", true))) {
                fileWriter.write(userId + "," + password);
                fileWriter.newLine();
            } catch (IOException e) {
                System.err.println("Error saving account.");
            }
        }

        private void processClientRequest(String request) {
            if (request.startsWith("MESSAGE,")) {
                processMessage(request);
            } else if (request.equals("ACTIVE_MEMBERS")) {
                sendActiveMembers();
            } else if (request.equals("READ_MESSAGES")) {
                sendStoredMessages();
            } else if (request.equals("READ_PRIVATE_MESSAGES")) {
            	sendPrivateMessages();
            }
        }

        private void processMessage(String request) {
            String[] parts = request.split(",", 3);
            if (parts.length < 3) return;

            String recipient = parts[1];
            String message = parts[2];

            saveMessage(userId, recipient, message);

            if (recipient.equals("everyone")) {
                for (PrintWriter pw : activeUsers.values()) {
                    pw.println(userId + ": " + message);
                }
            } else {
                PrintWriter recipientWriter = activeUsers.get(recipient);
                if (recipientWriter != null) {
                    recipientWriter.println(userId + ": " + message);
                }
            }
        }

        private void sendActiveMembers() {
            String members = String.join(", ", activeUsers.keySet());
            writer.println("ACTIVE_MEMBERS:" + members);
        }

        private void sendStoredMessages() {
            StringBuilder messages = new StringBuilder();
            try (BufferedReader fileReader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))) {
                String line;
                while ((line = fileReader.readLine()) != null) {
                    messages.append(line).append("\n");
                }
            } catch (IOException e) {
                System.err.println("Error reading chat history.");
            }
            writer.println("STORED_MESSAGES:" + messages.toString());
        }
        private void sendPrivateMessages() {
        	try (BufferedReader fileReader = new BufferedReader(new FileReader(CHAT_HISTORY_FILE))){
        		String line;
        		while((line = fileReader.readLine()) != null) {
        			if (line.contains("-> " + userId + ":")) {
        				writer.println(line);
        			}
        		}
        		writer.println("END_OF_MESSAGES");
        	} catch (IOException e) {
        		System.err.println("Error reading private messages. ");
        	}
        }

        private void saveMessage(String sender, String recipient, String message) {
            try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(CHAT_HISTORY_FILE, true))) {
                fileWriter.write(sender + " -> " + recipient + ": " + message);
                fileWriter.newLine();
            } catch (IOException e) {
                System.err.println("Error saving chat.");
            }
        }
    }
}
