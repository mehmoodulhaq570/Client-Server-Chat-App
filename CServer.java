    import java.io.*;
    import java.net.*;
    import java.util.ArrayList;
    import java.util.List;

    public class CServer {
        private static final int PORT = 7777;
        private static final List<ClientHandler> clients = new ArrayList<>();
        private static int clientCounter = 1;

        public static void main(String[] args) {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server is running... Ready to accept new connections.");


                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New connection: " + clientSocket + " --> CLient: " + clientCounter);

                    // Start a new thread to handle each client
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clients.add(clientHandler);
                    new Thread(clientHandler).start();
                }
            } catch (IOException e) {
                System.out.println("Error in connection with Client: " + clientCounter);
            }
        }

        private static class ClientHandler implements Runnable {
            private Socket clientSocket;
            private BufferedReader in;
            private PrintWriter out;
            private String clientName;

            public ClientHandler(Socket socket) {
                try {
                    this.clientSocket = socket;
                    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    this.out = new PrintWriter(socket.getOutputStream(), true);
                    this.clientName = "Client" + clientCounter;
                    clientCounter++; // Increment client counter for next client
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void run() {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Received from " + clientName + ": " + message);
            
                        if (message.startsWith("@")) {
                            // Private message format: @recipientName messageBody
                            String[] parts = message.split(" ", 2);
                            String recipientName = parts[0].substring(1); // Extract recipient's name
                            String privateMessage = clientName + " (private): " + parts[1];
                            sendPrivateMessage(recipientName, privateMessage);
                        } else {
                            // Broadcast message to all clients with client identifier
                            broadcast(clientName + ": " + message);
                        }
            
                        // Handle file request
                        if (message.startsWith("GET_FILE")) {
                            String fileName = message.split(" ")[1];
                            sendFile(fileName);
                        }
            
                        // Check if the client wants to exit
                        if (message.equals("exit")) {
                            System.out.println(clientName + " has exited");
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        clientSocket.close();
                        clients.remove(this); // Remove client handler from the list
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            
            private void sendPrivateMessage(String recipientName, String message) throws IOException {
                for (ClientHandler client : clients) {
                    if (client.clientName.equals(recipientName)) {
                        client.sendMessage(message);
                        break; // Send message to the first matched client (assuming client names are unique)
                    }
                }
            }
            

            private void broadcast(String message) throws IOException {
                // Loop through connected clients and send the message
                for (ClientHandler client : clients) {
                    if (client != this) {
                        client.sendMessage(message);
                    }
                }
            }

            private void sendMessage(String message) throws IOException {
                out.println(message);
            }

            private void sendFile(String fileName) throws IOException {
                File file = new File(fileName);
                if (!file.exists()) {
                    out.println("File not found");
                    return;
                }

                try (BufferedReader fileReader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = fileReader.readLine()) != null) {
                        out.println(line);
                    }
                }
                out.println("File sent successfully");
                return;

            }
        }
    }
