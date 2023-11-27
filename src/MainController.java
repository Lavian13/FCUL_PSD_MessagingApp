import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.application.Platform;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class MainController {

    BufferedReader reader = null;
    PrintWriter writer = null;
    SSLSocket sslSocket;
    public static String otherUsername;
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

    public void sendButtonPress(ActionEvent event) throws IOException {
        //CODE TO SEND MESSAGE TO ACTIVEIPS IN MAINCLASS
        sendMessage(messageField.getText());
        messageField.clear();
        sendButton.setStyle("-fx-background-color: red;");
    }


    @FXML
    public void initialize() throws IOException {
        System.out.println("works");

        for(int i=0; i<2;i++){
                //String columnName = resultSet.getString("column_name");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DynamicPane.fxml"));
                Pane contact = loader.load();

                DynamicController controller = loader.getController();
                //controller.setData(columnName);
                if(i%2==0)
                    controller.setData("DavidOliveira");
                else controller.setData("LuisViana");

                contact.setOnMouseClicked(event -> {
                    if(!controller.getData().equals(otherUsername)) {


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
                        otherUsername = controller.getData();
                        System.out.println(Peer.ipReceiver);
                        try {
                            loadChat(otherUsername);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        //connectToUser();//then delete the sleep
                        SSLSocket sslSocket = Peer.sslSocketUsers.get(otherUsername);
                        if (sslSocket == null) {
                            userOnline.setText("User Offline");

                        } else {
                            userOnline.setText("User Online");
                            reader = Peer.usersReaders.get(otherUsername);
                            try {
                                writer = new PrintWriter(sslSocket.getOutputStream(), true);
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
                });

                // Add the dynamic pane to the parent container
                contacts.getChildren().add(contact);

            }
 /*       } catch (SQLException | IOException e) {
            e.printStackTrace();
        }*/

    }


    private void loadChat(String username) throws IOException {
        /*FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane message = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData("hey");
        message.setOnMouseClicked(event -> {//TO REMOVE
            message.setStyle("-fx-background-color: red;");
        });*/
        if(Peer.username_Messages.size()!=0){
            for(String i : Peer.username_Messages.get(username)){
                FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
                AnchorPane message = loader2.load();
                MessageController controller2 = loader2.getController();
                controller2.setData(i);
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
                String aux = otherUsername;
                System.out.println("Threadreading" + otherUsername);
                Peer.notificationQueue.clear();
                while (true) {
                    // Wait for a notification
                    Peer.notificationQueue.take();
                    if (aux.equals(otherUsername)) {
                        String receivedMessage = Peer.username_Messages.get(aux).getLast();
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
        writer.println(text);
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

