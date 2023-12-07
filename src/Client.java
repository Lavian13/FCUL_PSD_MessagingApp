import javax.crypto.KeyAgreement;
import java.io.*;
import java.net.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.cert.X509Certificate;
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
            System.setProperty("javax.net.ssl.keyStore", "client_tls/client-keystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "client_tls/client-truststore.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");

            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 2345);
            sslSocket.setNeedClientAuth(true);
            System.out.println("Connected to server!");
            X509Certificate[] serverCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            if (serverCertificates.length > 0) {
                X509Certificate clientCertificate = serverCertificates[0]; // Assuming the client provides a certificate
                String subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);
            }
            // Create a PrintWriter to send a message to the server
            PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);
            writer.println("Hello, server!");
            String clientCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Client Cipher Suite: " + clientCipherSuite);
            String clientTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Client TLS Version: " + clientTLSVersion);
            // Create a BufferedReader to read the server's messages
            BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            String line = reader.readLine();
            System.out.println("Received from server: " + line);

            sslSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String extractSubjectCommonName(X509Certificate certificate) {
        String subjectDN = certificate.getSubjectX500Principal().getName();
        String[] dnComponents = subjectDN.split(",");
        for (String component : dnComponents) {
            if (component.trim().startsWith("CN=")) {
                // Extract the CN value
                return component.trim().substring(3);
            }
        }
        return "Unknown";
    }

}
