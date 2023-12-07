package cn;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    List<PrintWriter> writers = new ArrayList<>();
    SSLSocket sslSocket;
    public static String chatName;
    private Thread threadRead;
    @FXML
    private VBox contacts; // Reference to the parent container in the FXML file
    @FXML
    private VBox messages;
    @FXML
    private Button sendButton;
    @FXML
    private Label userOnline;
    @FXML
    private TextField messageField;
    @FXML
    private ToolBar toolBar;

    public void sendButtonPress(ActionEvent event) throws IOException {
        sendMessage(messageField.getText());
        messageField.clear();
        //sendButton.setStyle("-fx-background-color: red;");
    }


    @FXML
    public void initialize() throws IOException, InterruptedException {
        System.out.println("works");

        Thread.sleep(400);
        for(File file : Peer.listOfFiles){

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DynamicPane.fxml"));
                Pane contact = loader.load();

                DynamicController controller = loader.getController();
                if (file.getName().contains("_"))
                    controller.setData(file.getName().split("_")[1].split("\\.")[0]);
                else controller.setData(file.getName().split("\\.")[0]);

                contact.setOnMouseClicked(event -> {
                    if(!controller.getData().equals(chatName)) {


                        //HERE DO THE CODE TO LOAD THE CHAT IN THE RIGHT SIDE OF THE PAGE
                    /*if (reader!=null && writer!=null){
                        //guardarmensagens em ficheiro encriptado
                        writer.println("close");
                        try {
                            sslSocket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }*/

                    /*try {
                        Peer.sendMessageToServerUsername(controller.getData());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                        messages.getChildren().clear();
                        chatName = controller.getData();
                        //System.out.println(Peer.ipReceiver);
                        try {
                            loadChat(chatName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        List<String> usernames=new ArrayList<>();
                        if (Peer.groupUsers.containsKey(chatName)){
                            usernames.addAll(Peer.groupUsers.get(chatName));
                        }else{
                            usernames.add(chatName);
                        }
                        for (String user : usernames){
                            SSLSocket sslSocket = Peer.sslSocketUsers.get(user);
                            if (sslSocket == null) {
                                if (toolBar.isVisible()) toolBar.setVisible(false);
                                userOnline.setText("User Offline");

                            } else {
                                if (!toolBar.isVisible()) toolBar.setVisible(true);
                                userOnline.setText("User Online");
                                try {
                                    writers.add(new PrintWriter(sslSocket.getOutputStream(), true));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }

                                contact.setStyle("-fx-background-color: red;");
                                try {
                                    threadToRead();
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        }

                    }
                });

                // Add the dynamic pane to the parent container
                contacts.getChildren().add(contact);

            }
 /*       } catch (SQLException | IOException e) {
            e.printStackTrace();
        }*/

    }


    private void loadChat(String chatname) throws IOException {
        for (File f : Peer.listOfFiles){
            if (f.getName().contains(chatname)){
                if (f.getName().split("_")[1].equals(chatName+".txt")){

                }
            }else{

            }
        }
        /*FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane message = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData("hey");
        message.setOnMouseClicked(event -> {//TO REMOVE
            message.setStyle("-fx-background-color: red;");
        });*/
        //if(Peer.username_Messages.size()!=0){
        if(Peer.messages.containsKey(chatname)){
            //for(String i : Peer.username_Messages.get(username)){
            for(Message i : Peer.messages.get(chatname)){
                String content = i.getContent();

                FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
                AnchorPane message = loader2.load();
                MessageController controller2 = loader2.getController();
                controller2.setData(content);

                if(i.getSent()) controller2.setPaneRightSide();

                messages.getChildren().add(message);
            }
        }

        //messages.getChildren().add(message);
    }

    /*private void connectToUser(){
        try {
            if(Peer.ipReceiver==null || Peer.ipReceiver.equals("")|| Peer.ipReceiver.isEmpty() || Peer.ipReceiver.equals("null")) {
                userOnline.setText("User doesn't exist anymore");
                return;
            }else userOnline.setText("User Online");

            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            String[] ip_port = Peer.ipReceiver.split(":");
            sslSocket = (SSLSocket) sslSocketFactory.createSocket(ip_port[0], Integer.parseInt(ip_port[1]));
            sslSocket.setNeedClientAuth(true);
            System.out.println("Connected to user!");
            X509Certificate[] serverCertificates = (X509Certificate[]) sslSocket.getSession().getPeerCertificates();
            if (serverCertificates.length > 0) {
                X509Certificate clientCertificate = serverCertificates[0]; // Assuming the client provides a certificate
                String subjectCN = extractSubjectCommonName(clientCertificate);
                System.out.println("The first name of the person's certificate -> " + subjectCN);
            }

            System.out.println("Connected to user: " + sslSocket);

            // Set up input and output streams for communication
            //reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            writer = new PrintWriter(sslSocket.getOutputStream(), true);



            //threadToRead();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }*/


    private void threadToRead() throws IOException {
        Task<Void> backgroundTask = new Task<>(){

            @Override
            protected Void call() throws Exception {
                String aux = chatName;
                System.out.println("Threadreading" + chatName);
                Peer.notificationQueue.clear();
                while (true) {
                    // Wait for a notification
                    Peer.notificationQueue.take();
                    if (aux.equals(chatName)) {
                        String receivedMessage = Peer.messages.get(aux).getLast().getContent();
                        Platform.runLater(() -> {
                            try {
                                receiveMessage(receivedMessage);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });

                        System.out.println("Received: " + receivedMessage);
                    } else {
                        Peer.notificationQueue.put(true);
                        break;
                    }
                }
                return null;
            }
        };
        threadRead = new Thread(backgroundTask);
        threadRead.setDaemon(true);
        threadRead.start();
        /*Thread readingThread = new Thread(() -> {
            try {
                String aux = otherUsername;
                System.out.println("Threadreading" + otherUsername);
                while (true) {
                    // Wait for a notification
                    Peer.notificationQueue.take();
                    if(aux.equals(otherUsername)){
                        String receivedMessage = Peer.username_Messages.get(aux).getLast();
                        Platform.runLater(() ->receiveMessage(receivedMessage));

                        System.out.println("Received: " + receivedMessage);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        readingThread.start();*/


    }

    private void sendMessage(String text) throws IOException {
        for (PrintWriter writer : writers){
            writer.println(text);
        }
        System.out.println("Message sent to:" + Peer.groupUsers.get(chatName));
        if(Peer.groupUsers.get(chatName)==null)
            Peer.messages.get(chatName).add(new Message(true, List.of(chatName), text));
        else Peer.messages.get(chatName).add(new Message(true, Peer.groupUsers.get(chatName), text));

        loadMessageUI(text);
    }
    public void receiveMessage(String text) throws IOException {
        loadOtherMessageUI(text);

    }

    private void loadMessageUI(String message) throws IOException {
        System.out.println("adding to ui sent message");
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane messagePane = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData(message);
        messagePane.setOnMouseClicked(event -> {//TO REMOVE
            messagePane.setStyle("-fx-background-color: red;");
        });
        controller2.setPaneRightSide();
        messages.getChildren().add(messagePane);
    }
    private void loadOtherMessageUI(String message) throws IOException {
        System.out.println("adding to ui received message");
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane messagePane = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData(message);

        //put message on the right side

        messages.getChildren().add(messagePane);
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

