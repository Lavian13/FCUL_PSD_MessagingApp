import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;

// Server class
public class Peer extends Thread  {

    public static void main(String[] args){
        Peer peer = new Peer();
        peer.start();
    }
    @Override
    public void run() {
        try {
            System.setProperty("javax.net.ssl.keyStore", "server_tls/server-keystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "server_tls/server-truststore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "123456");
            System.setProperty("javax.net.ssl.trustStorePassword", "123456");

            SSLContext sslContext = SSLContext.getDefault();
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(2345);
            sslServerSocket.setNeedClientAuth(true);
            System.out.println("Waiting for client connection...");

            // Start the server
            Thread serverThread = new Thread(new ServerThread(sslServerSocket));
            serverThread.start();

            // Connecting to another server
            Thread clientThread = new Thread(new ClientThread("localhost", 9090));
            clientThread.start();

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

    class ServerThread implements Runnable {
        private SSLServerSocket socket;

        public ServerThread(SSLServerSocket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                while (true) {
                    SSLSocket sslSocket = (SSLSocket) socket.accept();
                    System.out.println("New connection accepted: " + sslSocket);

                    // Start a new thread to handle the client
                    Thread clientThread = new Thread(new ClientHandler(sslSocket));
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class ClientThread implements Runnable {
        private String serverAddress;
        private int serverPort;

        public ClientThread(String address, int port) {
            this.serverAddress = address;
            this.serverPort = port;
        }

        public void run() {
            try {
                SSLContext sslContext = SSLContext.getDefault();
                SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
                SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
                sslSocket.setNeedClientAuth(true);
                System.out.println("Connected to server!");
                X509Certificate[] serverCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
                if (serverCertificates.length > 0) {
                    X509Certificate clientCertificate = serverCertificates[0]; // Assuming the client provides a certificate
                    String subjectCN = extractSubjectCommonName(clientCertificate);
                    System.out.println("The first name of the person's certificate -> " + subjectCN);
                }

                System.out.println("Connected to server: " + sslSocket);


                // Set up input and output streams for communication
                BufferedReader reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                PrintWriter writer = new PrintWriter(sslSocket.getOutputStream(), true);

                // Send a message to the connected server
                writer.println("Hello from server!");

                reader.close();
                writer.close();
                sslSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }
    }

    class ClientHandler implements Runnable {
        private SSLSocket sslSocket;

        public ClientHandler(SSLSocket sslSocket) {
            this.sslSocket = sslSocket;
        }

        public void run() {
            System.out.println("Client connected!");
            X509Certificate[] clientCertificates = new X509Certificate[0];
            try {
                clientCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                throw new RuntimeException(e);
            }
            if (clientCertificates.length > 0) {
                X509Certificate clientCertificate = clientCertificates[0]; // Assuming the client provides a certificate
                String subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);
            }
            // Create a BufferedReader to read the client's messages
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String line = null;
            try {
                line = reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Received from client: " + line);
            String serverCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Server Cipher Suite: " + serverCipherSuite);
            String serverTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Server TLS Version: " + serverTLSVersion);
            // Create a PrintWriter to send a message to the client
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(sslSocket.getOutputStream(), true);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            writer.println("Hello, client!");

            try {
                sslSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}