import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;

public class Server {

    HashMap<String,String> clientIps = new HashMap<>();

    public static void main(String[] args) {
        try {

            /*SSLServerSocketFactory sslServerSocketFactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            SSLServerSocket serverSocket = (SSLServerSocket) sslServerSocketFactory.
                    createServerSocket(12345);
            SSLSocket sslSocket = (SSLSocket) serverSocket.accept();*/
            // Create a server socket listening on port 12345
            ServerSocket serverSocket = new ServerSocket(3456);
            System.out.println("Server started. Waiting for a client to connect...");

            // Wait for a client to connect
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            // Create input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read message from client and print it
            String message = in.readLine();
            System.out.println("Received message from client: " + message);

            // Send a response back to the client
            out.println("Message received by the server: " + message);

            // Close the streams and sockets
            in.close();
            out.close();
            clientSocket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
