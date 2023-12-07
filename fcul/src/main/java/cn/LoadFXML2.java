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
import java.net.URL;
import java.util.*;


public class LoadFXML2 extends Application{
    public HashMap<String, String> username_ip = new HashMap<>();
    private List<String> ips = new ArrayList<>();

    public static void main(String[] args) {

        Peer peer = new Peer("Alice");
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

    }

    private void closeWindowEvent(WindowEvent windowEvent) {
        ObjectMapper objectMapper = new ObjectMapper();
        for (String chatName : Peer.messages.keySet()){
            File file = new File("src/main/java/cn/chatsMessages/" + chatName + ".txt");
            System.out.println("SIZE" + Peer.messages.get(chatName).size());
            for (Message message : Peer.messages.get(chatName)){
                try (FileWriter fileWriter = new FileWriter(file, true)) {
                    // The 'true' parameter in the FileWriter constructor enables append mode
                    objectMapper.writeValue(fileWriter, message);
                    objectMapper.writeValue(fileWriter, "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}