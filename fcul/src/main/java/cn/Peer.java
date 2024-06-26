package cn;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.net.ssl.*;

// Server class
public class Peer extends Thread  {

    private static final Object serverLock = new Object();
    private static boolean condition = false;
    public static String usernameReceiver;
    public static String ipReceiver;
    //public static String messageToServer;
    private static BufferedReader serverReader;
    private static PrintWriter serverWriter;
    private static SSLSocket sslSocket = null;
    public static HashMap<String, SSLSocket> sslSocketUsers = new HashMap<>();//connections of evryone i have a chat with
    public static HashMap<String, BufferedReader> usersReaders = new HashMap<>();
    public static HashMap<String, List<String>> username_Messages = new HashMap<>();
    private int user;
    public static final BlockingQueue<Boolean> notificationQueue = new LinkedBlockingQueue<>();
    public static HashMap<String, List<Message>> messages = new HashMap<>();


    public Peer(int user){
        this.user=user;
        if(user==1){
            System.setProperty("javax.net.ssl.keyStore", "src/Luis_cert/luiskeystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "src/Luis_cert/luistruststore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "luispass");
            System.setProperty("javax.net.ssl.trustStorePassword", "luispass");
        }else if(user==2){
            System.setProperty("javax.net.ssl.keyStore", "src/David_cert/davidkeystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "src/David_cert/davidtruststore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "davidpass");
            System.setProperty("javax.net.ssl.trustStorePassword", "davidpass");
        }

    }

    @Override
    public void run() {
        try {
            /*System.setProperty("javax.net.ssl.keyStore", "src/Luis_cert/luiskeystore.jks");
            System.setProperty("javax.net.ssl.trustStore", "src/Luis_cert/luistruststore.jks");
            System.setProperty("javax.net.ssl.keyStorePassword", "luispass");
            System.setProperty("javax.net.ssl.trustStorePassword", "luispass");*/

            SSLContext sslContext = SSLContext.getDefault();
            SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

            SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(2344+user);
            sslServerSocket.setNeedClientAuth(true);

            ConnectToServer("localhost", 9090, usernameReceiver);
            System.out.println("port:" + (2344+user));
            serverWriter.println("port:" + (2344+user));
            serverReader.readLine();


            System.out.println("Waiting for client connection...");
        //for all users i have a group chat with ask the ip to the server
            String otherUsername="";
            if(user==1)
                otherUsername = "DavidOliveira";
            else
                otherUsername="LuisViana";
            sendMessageToServerUsername(otherUsername);
            //if ipReceiver==null continue for
            if (ipReceiver.split(":").length==2) {
                try {
                    sslSocket=null;
                    SSLContext sslContext_ = SSLContext.getDefault();
                    SSLSocketFactory sslSocketFactory = sslContext_.getSocketFactory();
                    String[] ip_port = ipReceiver.split(":");
                    System.out.println("hey" + ip_port[0] + " "+ Integer.parseInt(ip_port[1]));
                    sslSocket = (SSLSocket) sslSocketFactory.createSocket(ip_port[0], Integer.parseInt(ip_port[1]));
                    sslSocket.setNeedClientAuth(true);
                    System.out.println("Connected to user!");
                    X509Certificate[] serverCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
                    if (serverCertificates.length > 0) {
                        X509Certificate clientCertificate = serverCertificates[0]; // Assuming the client provides a certificate
                        String subjectCN = extractSubjectCommonName(clientCertificate);
                        System.out.println("The first name of the person's certificate -> " + subjectCN);
                        sslSocketUsers.put(subjectCN, sslSocket);
                    }

                    System.out.println("Connected to user: " + sslSocket);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException(e);
                }

            }

            if (sslSocket != null) {
                Thread clientThread = new Thread(new ClientHandler(sslSocket));
                clientThread.start();
            }

            messages.put(otherUsername, new ArrayList<>());

        //endoffor


            // Start listening to connections
            Thread serverThread = new Thread(new ServerThread(sslServerSocket));
            serverThread.start();


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
        //System.out.println(usernameReceiver + " " + ipReceiver);

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
                    System.out.println("got here");
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
            String subjectCN=null;
            try {
                clientCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                throw new RuntimeException(e);
            }
            if (clientCertificates.length > 0) {
                X509Certificate clientCertificate = clientCertificates[0]; // Assuming the client provides a certificate
                subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);
                sslSocketUsers.put(subjectCN,sslSocket);
            }
            // Create a BufferedReader to read the client's messages
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
                usersReaders.put(subjectCN,reader);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            String serverCipherSuite = sslSocket.getSession().getCipherSuite();
            System.out.println("Server Cipher Suite: " + serverCipherSuite);
            String serverTLSVersion = sslSocket.getSession().getProtocol();
            System.out.println("Server TLS Version: " + serverTLSVersion);
            // Create a PrintWriter to send a message to the client
            try {
                while (true) {
                    System.out.println(reader);
                    String receivedMessage = reader.readLine();

                    if (receivedMessage == "close") {
                        sslSocket.close();
                        break; // Connection closed
                    }//else do a notification and write to the hashmap of messages
                    else{
                        if(subjectCN!=null){
                            System.out.println(subjectCN + " username" + MainController.otherUsername);
                            /*if (username_Messages.containsKey(subjectCN))
                                username_Messages.get(subjectCN).add(receivedMessage);
                            else {
                                username_Messages.put(subjectCN, new ArrayList<>());
                                username_Messages.get(subjectCN).add(receivedMessage);
                            }*/
                            messages.get(subjectCN).add(new Message(false,subjectCN, receivedMessage)); //get wont be subjectCN
                            notificationQueue.offer(true);

                            /*if(subjectCN.equals(MainController.otherUsername)){
                                changeui
                            }*/
                            System.out.println("Received: " + receivedMessage);
                        }

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }



        }
    }


}
