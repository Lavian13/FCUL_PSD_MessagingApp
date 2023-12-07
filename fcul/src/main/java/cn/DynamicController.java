package cn;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class DynamicController {

    @FXML
    private Label label; // Example component in the dynamic pane
    @FXML
    private Pane contact;

    @FXML
    public void initialize() throws IOException {

    }

    // Method to set data in the dynamic pane components
    public void setData(String data) {
        label.setText(data);
    }

    public String getData(){ return label.getText();}

    public void setColorLighter(){
        contact.setStyle("-fx-background-color:#5e6397");
    }
    public void setColorNormal(){
        contact.setStyle("-fx-background-color: #4d528c");

    }
    public void selectContact(){

    }
}
