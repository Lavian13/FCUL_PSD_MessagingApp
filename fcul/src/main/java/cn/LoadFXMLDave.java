package cn;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.util.*;


public class LoadFXMLDave extends Application{
    public HashMap<String, String> username_ip = new HashMap<>();
    private List<String> ips = new ArrayList<>();
    private static String username = "Dave";

    public static void main(String[] args) {

        Peer peer = new Peer(username);
        peer.start();
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
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
            String fileName= "chatsMessages/" +username +"/"+ chatName + ".txt";
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