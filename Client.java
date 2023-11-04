import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.*;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.util.Scanner;

// Client class
class Client {


    // driver code
    public static void main(String[] args) throws Exception {

        try {
            System.setProperty("javax.net.ssl.trustStore", "truststore.p12");
            System.setProperty("javax.net.ssl.trustStorePassword", "iDcwJk$&TzgJ4NGMn2%M");

            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 1234);

            System.out.println("Connected to server!");

            // Create a PrintWriter to send a message to the server
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            writer.println("Hello, server!");

            // Create a BufferedReader to read the server's messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String line = reader.readLine();
            System.out.println("Received from server: " + line);

            sslSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}


