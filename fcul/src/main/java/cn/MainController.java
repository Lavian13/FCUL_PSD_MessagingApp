package cn;

import cn.edu.buaa.crypto.access.parser.PolicySyntaxException;
import cn.edu.buaa.crypto.algebra.serparams.PairingCipherSerParameter;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.controlsfx.control.Notifications;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static cn.App.deserCipherParameters;

public class MainController {

    List<PrintWriter> writers = new ArrayList<>();
    List<SSLSocket> sslSockets= new ArrayList<>();
    public static String chatName;
    public static String filename;
    private List<DynamicController> controllers = new ArrayList<>();
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
    @FXML
    private ScrollPane messageScroll;

    public void sendButtonPress(ActionEvent event) throws IOException, InvalidCipherTextException, PolicySyntaxException, ClassNotFoundException {
        sendMessage(messageField.getText());
        messageField.clear();
        //sendButton.setStyle("-fx-background-color: red;");
    }


    @FXML
    public void initialize() throws IOException, InterruptedException {
        System.out.println("works");

        Thread.sleep(400);
        threadToRead();
        for(File file : Peer.listOfFiles){

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DynamicPane.fxml"));
                Pane contact = loader.load();

                DynamicController controller = loader.getController();
                controllers.add(controller);
                if (file.getName().contains("_"))
                    controller.setData(file.getName().split("_")[1].split("\\.")[0]);
                else controller.setData(file.getName().split("\\.")[0]);
                controller.setFilename(file.getName().split("\\.")[0]);
                System.out.println(controller.getFilename());

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
                        //Platform.runLater(controller::resetCounter);
                        Platform.runLater(() -> {
                           controller.resetCounter();
                           for(DynamicController cont:controllers){
                               cont.deselect();
                           }
                           controller.select();
                           System.out.println("changecolor");
                        });
                        messages.getChildren().clear();
                        writers.clear();
                        sslSockets.clear();
                        chatName = controller.getData();
                        filename=controller.getFilename();
                        //System.out.println(Peer.ipReceiver);
                        try {
                            loadChat(filename);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        List<String> usernames=new ArrayList<>();
                        if (Peer.groupUsers.containsKey(chatName)){
                            //usernames.addAll(Peer.groupUsers.get(chatName));
                            usernames.addAll(Peer.usersReaders.keySet());
                        }else{
                            usernames.add(filename);
                        }
                        for (String username : usernames) {
                            System.out.println(username);
                        }
                        //System.out.println("groupusers found" + Peer.groupUsers.get(chatName));
                        boolean oneUserOffline=false;
                        for (String user : usernames){
                            SSLSocket sslSocket = Peer.sslSocketUsers.get(user);
                            if (sslSocket == null) {
                                oneUserOffline=true;
                            }else sslSockets.add(sslSocket);
                        }
                        if (oneUserOffline){
                            if (!toolBar.isDisable()) toolBar.setDisable(true);
                            userOnline.setText("Chat is Offline");
                        } else {
                            if (toolBar.isDisable()) toolBar.setDisable(false);
                            userOnline.setText("Chat is Online");
                            for (SSLSocket s :sslSockets){
                                try {
                                    writers.add(new PrintWriter(s.getOutputStream(), true));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
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


    private void loadChat(String filename) throws IOException {
        for (File f : Peer.listOfFiles){
            if (f.getName().equals(filename)){
                /*try (BufferedReader reader = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Read each line as a JSON object and convert it to a Java object
                        System.out.println(line);
                        String[] messageAttributes = line.split(",");
                        if(messageAttributes[0].equals("false")) receiveMessage(messageAttributes[1]);
                        else loadMessageUI(messageAttributes[1]);
                        //System.out.println("Name: " + message.getName() + ", Age: " + message.getAge());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                try {
                    for (String message: DownloadShares.decryptMessages("chatsMessages/" + Peer.userName +"/"+ f.getName())){
                        String[] messageAttributes = message.split(",");
                        if(messageAttributes[0].equals("false")) receiveMessage(messageAttributes[1]);
                        else loadMessageUI(messageAttributes[1]);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
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
        if(Peer.messages.containsKey(filename)){
            //for(String i : Peer.username_Messages.get(username)){
            for(Message i : Peer.messages.get(filename)){
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
                    System.out.println("aftertake");
                    if (chatName==null){
                        System.out.println("Mensagem recebida de null" + Peer.lastMessageReceived.getFileName());
                        notiMessage(Peer.lastMessageReceived.getFileName());
                    }
                    else if(aux==null&&chatName!=null){
                        Peer.notificationQueue.put(true);
                        System.out.println("onbreaknull");
                        break;
                    }
                    else if (aux.equals(chatName)) {
                        if(Peer.lastMessageReceived.getFileName().equals(filename) && !Peer.lastMessageReceived.getSent()){
                            String receivedMessage = Peer.lastMessageReceived.getContent();
                            Platform.runLater(() -> {
                                try {
                                    receiveMessage(receivedMessage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                            System.out.println("chatname: " + chatName);
                            System.out.println("Receivedmaincontroller: " + receivedMessage);
                        }else{
                            System.out.println("Mensagem recebida de" + Peer.lastMessageReceived.getFileName());
                            notiMessage(Peer.lastMessageReceived.getFileName());

                        }

                    } else {
                        Peer.notificationQueue.put(true);
                        System.out.println("onbreak");

                        break;
                    }
                }
                System.out.println("afterbreak");


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

    public void notiMessage(String filename) {
        for (DynamicController dc:controllers){
            if(dc.getFilename().equals(filename)){
                Platform.runLater(dc::incrementCounter);
                break;
            }

        }
    }

    private void sendMessage(String text) throws IOException, PolicySyntaxException, ClassNotFoundException, InvalidCipherTextException {
        System.out.println("Writers" + writers.size());
        //System.out.println("Message sent to:" + Peer.groupUsers.get(chatName));
        if(Peer.groupUsers.get(chatName)==null){
            Peer.messages.get(filename).add(new Message(true, filename, Peer.userName, text));
            for (PrintWriter writer : writers){
                //writer.println(text);
                writer.println(Peer.userName + "," + text);
            }
        }
        else {
            App.defineAccessPolicyString("40 and (200 or 430 or 30)");
            Peer.messages.get(filename).add(new Message(true, filename, Peer.userName, text));
            System.out.println("Mesnsagem escrita"+text);
            String enctext= App.encryptStringPublic(text, Peer.group_accessstring.get(filename),Peer.attribute_publickey.get(chatName));
            System.out.println("Encrypted"+enctext);
            System.out.println("Secretkeydecr"+Peer.secretKey.getParameters());
            System.out.println("decrypted"+App.decryptStringPublic(enctext,Peer.secretKey, Peer.group_accessstring.get(filename),Peer.attribute_publickey.get(chatName)));

            for (PrintWriter writer : writers){
                writer.println(filename + "," + enctext);
            }
        }

        loadMessageUI(text);
    }
    public void receiveMessage(String text) throws IOException {
        loadOtherMessageUI(text);

    }

    private void loadMessageUI(String message) throws IOException {
        //System.out.println("adding to ui sent message");
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane messagePane = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData(message);
        controller2.setPaneRightSide();
        messages.getChildren().add(messagePane);
        //messageScroll.setVvalue(1.0);
    }
    private void loadOtherMessageUI(String message) throws IOException {
        //System.out.println("adding to ui received message");
        FXMLLoader loader2 = new FXMLLoader(getClass().getResource("Message.fxml"));
        AnchorPane messagePane = loader2.load();
        MessageController controller2 = loader2.getController();
        controller2.setData(message);


        //put message on the right side

        messages.getChildren().add(messagePane);
        //messageScroll.setVvalue(1.0);

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

