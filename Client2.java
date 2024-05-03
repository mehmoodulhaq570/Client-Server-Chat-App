import java.io.*;
import java.net.*;

public class Client2 {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 7777;

    public static void main(String[] args) {
        Socket socket = null;
        BufferedReader userInputReader = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            // Establish connection to the server
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Successfully connected to server!");
            System.out.println("Enter your message below.");

            // Setup input and output streams
            userInputReader = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Start a thread to listen for messages from the server
            final BufferedReader finalIn = in; // Declare 'in' as final
            Thread receiveThread = new Thread(() -> {
                try {
                    String serverMessage;
                    while ((serverMessage = finalIn.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    System.err.println("Error in reading from server");
                    e.printStackTrace();
                }
            });
            receiveThread.start();

            // Read user input and send messages to the server
            String userInput;
            while ((userInput = userInputReader.readLine()) != null) {
                if (userInput.equals("exit")) {
                    System.out.println("Connection with server has been terminated");
                    break; // Exit the loop if "exit" command is entered
                }

                // Check if the message is a private message
                if (userInput.startsWith("@")) {
                    // Extract recipientName and messageBody
                    int spaceIndex = userInput.indexOf(" ");
                    if (spaceIndex != -1) {
                        String recipientName = userInput.substring(1, spaceIndex);
                        String messageBody = userInput.substring(spaceIndex + 1);
                        String privateMessage = "@" + recipientName + " " + messageBody;
                        out.println(privateMessage); // Send private message to the server
                    } else {
                        System.out.println("Invalid private message format. Usage: @recipientName messageBody");
                    }
                } else {
                    out.println(userInput); // Send regular message to the server
                }
            }
        } catch (IOException e) {
            System.err.println("Error connecting to the server");
            e.printStackTrace();
        } finally {
            try {
                // Close all resources in the finally block
                if (out != null) {
                    out.close();
                }
                if (userInputReader != null) {
                    userInputReader.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Error closing resources");
                e.printStackTrace();
            }
            System.out.println("Disconnected from server");
        }
    }
}
