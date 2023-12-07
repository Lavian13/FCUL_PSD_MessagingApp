package cn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;


public class LoadFXMLCharlie extends Application{
    public HashMap<String, String> username_ip = new HashMap<>();
    private List<String> ips = new ArrayList<>();
    private static String username = "Charlie";

    public static void main(String[] args) {

        Peer peer = new Peer(username);
        peer.start();
        launch(args);
        //RetrieveIPThread ip_thread = new RetrieveIPThread("127.0.0.1", 3456);
        //ip_thread.start();

    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        //FXMLLoader loader = new FXMLLoader();
        //loader.setLocation(new URL("MessageAppUI.fxml"));
        //VBox vbox = loader.<VBox>load();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MessageAppUI.fxml"));
        Parent vbox = loader.load();

        Scene scene = new Scene(vbox);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
        primaryStage.setTitle(username);

    }

    private void closeWindowEvent(WindowEvent windowEvent) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (String chatName : Peer.messages.keySet()){
            //File file = new File("chatsMessages/" + chatName + ".txt");
            String fileName= "chatsMessages/" + chatName + ".txt";
            List<String> messages = new ArrayList<>();
            System.out.println("SIZE" + Peer.messages.get(chatName).size());
            for (Message message : Peer.messages.get(chatName)){
                messages.add(message.toString());
                /*try (PrintWriter writer = new PrintWriter(new FileWriter(file,true))) {
                    writer.println(message.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
            try {
                DownloadShares.encryptMessage(fileName,messages);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
