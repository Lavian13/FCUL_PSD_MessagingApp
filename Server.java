import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.nio.charset.StandardCharsets;

// Server class
class Server {

    public static void main(String[] args) throws Exception {

        try {
            System.setProperty("javax.net.ssl.keyStore", "keystore.p12");
            System.setProperty("javax.net.ssl.keyStorePassword", "iDcwJk$&TzgJ4NGMn2%M");

            SSLContext sslContext = SSLContext.getDefault();
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(1234);
            System.out.println("Waiting for client connection...");
            SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();

            System.out.println("Client connected!");

            // Create a BufferedReader to read the client's messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String line = reader.readLine();
            System.out.println("Received from client: " + line);
            String serverCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Server Cipher Suite: " + serverCipherSuite);
            String serverTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Server TLS Version: " + serverTLSVersion);
            // Create a PrintWriter to send a message to the client
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            writer.println("Hello, client!");

            sslSocket.close();
            sslServerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
