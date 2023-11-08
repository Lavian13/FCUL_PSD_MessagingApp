import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

import java.awt.*;
import java.io.IOException;
import java.sql.*;

public class MainController {


    @FXML
    private VBox contacts; // Reference to the parent container in the FXML file

    @FXML
    private Button sendButton;

    public void sendButtonPress(ActionEvent event){
        //CODE TO SEND MESSAGE TO ACTIVEIPS IN MAINCLASS
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





        for(int i=0; i<40;i++){
                //String columnName = resultSet.getString("column_name");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("DynamicPane.fxml"));
                Pane contact = loader.load();
                contact.setOnMouseClicked(event -> {
                    //HERE DO THE CODE TO LOAD THE CHAT IN THE RIGHT SIDE OF THE PAGE
                    contact.setStyle("-fx-background-color: red;"); // Change the color to red when clicked
                });


                DynamicController controller = loader.getController();
                //controller.setData(columnName);
                controller.setData("hey" + i);


                // Add the dynamic pane to the parent container
                contacts.getChildren().add(contact);
            }
 /*       } catch (SQLException | IOException e) {
            e.printStackTrace();
        }*/

    }
}

