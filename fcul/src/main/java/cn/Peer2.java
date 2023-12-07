package cn;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.*;

// Server class
public class Peer2 extends Thread  {

    private static final Object serverLock = new Object();
    private static boolean condition = false;
    public static String usernameReceiver;
    public static String ipReceiver;
    //public static String messageToServer;
    private static BufferedReader serverReader;
    private static PrintWriter serverWriter;
    private static SSLSocket sslSocket;
    private static List<SSLSocket> sslSocketUsers;



    public static void main(String[] args){
        Peer2 peer = new Peer2();
        peer.start();
    }
    @Override
    public void run() {
        try {
            System.setProperty("javax.net.ssl.keyStore", "src/David_cert/davidkeystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "src/David_cert/davidtruststore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "davidpass");
            System.setProperty("javax.net.ssl.trustStorePassword", "davidpass");

            SSLContext sslContext = SSLContext.getDefault();
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(2346);
            sslServerSocket.setNeedClientAuth(true);
            System.out.println("Waiting for client connection...");

            // Start the server
            Thread serverThread = new Thread(new ServerThread(sslServerSocket));
            serverThread.start();

            ConnectToServer("localhost", 9090, usernameReceiver);

            // Connecting to another server
            serverWriter.println("port:2345");
            serverReader.readLine();
            /*ipReceiver = serverReader.readLine();
            System.out.println("Message received" + ipReceiver);
            serverWriter.println("close");
            serverReader.close();
            serverWriter.close();
            sslSocket.close();
            System.out.println("closed");*/


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public static void sendMessageToServer(String message) throws IOException {
        serverWriter.println(message);
        serverReader.readLine();
        messageToServer="request:username:" + usernameReceiver;

    }*/

    public static void sendMessageToServerUsername(String username) throws IOException {
        String messageToServer="request:username:" + username;
        usernameReceiver=username;
        serverWriter.println(messageToServer);
        String read = serverReader.readLine();
        ipReceiver=read;

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


    public void ConnectToServer(String serverAddress, int serverPort, String username) {
        try {
            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(serverAddress, serverPort);
            sslSocket.setNeedClientAuth(true);
            System.out.println("Connected to server!");
            X509Certificate[] serverCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            if (serverCertificates.length > 0) {
                X509Certificate clientCertificate = serverCertificates[0]; // Assuming the client provides a certificate
                String subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);
            }

            System.out.println("Connected to server: " + sslSocket);

            serverReader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            serverWriter = new PrintWriter(sslSocket.getOutputStream(), true);


        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
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
