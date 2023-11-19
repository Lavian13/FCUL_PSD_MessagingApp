import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.sql.*;

public class MainController {

    BufferedReader reader;
    PrintWriter writer;

    @FXML
    private VBox contacts; // Reference to the parent container in the FXML file
    @FXML
    private VBox messages;
    @FXML
    private Button sendButton;
    @FXML
    private Label userOnline;

    public void sendButtonPress(ActionEvent event){
        //CODE TO SEND MESSAGE TO ACTIVEIPS IN MAINCLASS
        sendMessage();
        sendButton.setStyle("-fx-background-color: red;");
    }


    @FXML
    public void initialize() throws IOException {
        System.out.println("works");

/*        try (Connection connection = DriverManager.getConnection("jdbc:mysql://hostname:port/database", "username", "password")) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM your_table_name");

            // Iterate through the result set and create panes dynamically
            while (resultSet.next()) {*/





        for(int i=0; i<15;i++){
                //String columnName = resultSet.getString("column_name");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DynamicPane.fxml"));
                Pane contact = loader.load();

                DynamicController controller = loader.getController();
                //controller.setData(columnName);
                controller.setData("David");

                contact.setOnMouseClicked(event -> {
                    //HERE DO THE CODE TO LOAD THE CHAT IN THE RIGHT SIDE OF THE PAGE
                    Peer.unlockServerUsername(controller.getData());
                    connectToUser();//then delete the sleep
                    contact.setStyle("-fx-background-color: red;");
                    try {
                        Thread.sleep(100);
                        loadChat(controller.getData());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
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
        if(Peer.ipReceiver==null || Peer.ipReceiver.equals("")|| Peer.ipReceiver.isEmpty() || Peer.ipReceiver.equals("null")) {
            userOnline.setText("User Offline");
        }
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane message = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData("hey");
        message.setOnMouseClicked(event -> {//TO REMOVE
            message.setStyle("-fx-background-color: red;");
        });
        messages.getChildren().add(message);
    }

    private void connectToUser(){
        try {
            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            String[] ip_port = Peer.ipReceiver.split(":");
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(ip_port[0], Integer.parseInt(ip_port[1]));
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
            reader = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
            writer = new PrintWriter(sslSocket.getOutputStream(), true);



            /*writer.println(messageToServer);
            ipReceiver = reader.readLine();
            System.out.println("Message received" + ipReceiver);
            writer.println("close");
            reader.close();
            writer.close();
            sslSocket.close();
            System.out.println("closed");*/

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    private void sendMessage() {
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

