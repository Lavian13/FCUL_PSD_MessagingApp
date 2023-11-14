import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.*;


public class LoadFXML extends Application{
    public HashMap<String, String> username_ip = new HashMap<>();
    private List<String> ips = new ArrayList<>();

    public static void main(String[] args) {
        launch(args);
        //RetrieveIPThread ip_thread = new RetrieveIPThread("127.0.0.1", 3456);
        //ip_thread.start();
        //Peer peer = new Peer();
        //peer.start();
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
    }
}