import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.sql.*;

public class MainController {

    @FXML
    private VBox contacts; // Reference to the parent container in the FXML file

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
                Pane dynamicPane = loader.load();


                DynamicController controller = loader.getController();
                //controller.setData(columnName);
                controller.setData("hey" + i);


                // Add the dynamic pane to the parent container
                contacts.getChildren().add(dynamicPane);
            }
 /*       } catch (SQLException | IOException e) {
            e.printStackTrace();
        }*/

    }
}

