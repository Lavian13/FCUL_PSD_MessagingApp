import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) throws IOException {


        URL whatismyip = new URL("http://ipecho.net/plain");
        BufferedReader in2 = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));

        String ip = in2.readLine();
        System.out.println(ip);
        //String ip = "2001:8a0:6a67:ca00:b853:60cd:b13b:4d24";
        try {

            //SSLSocketFactory sslSocketFactory =
            //        (SSLSocketFactory) SSLSocketFactory.getDefault();
            //SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket("2001:8a0:6a67:ca00:b853:60cd:b13b:4d24", 12345);

            // Connect to the server using its IP address and port number
            Socket socket = new Socket("85.240.136.254", 3456);


            // Create input and output streams for communication
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Send a message to the server
            out.println("Hello, server! This is a message from the client.");

            // Read and print the server's response
            String response = in.readLine();
            System.out.println("Server response: " + response);

            // Close the streams and socket
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
