import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.io.IOException;

public class MessageController {

    @FXML
    private Label label; // Example component in the dynamic pane
    @FXML
    private Pane messagePane;

    @FXML
    public void initialize() throws IOException {

    }

    public void setData(String data) {
        label.setText(data);
    }

    public void setPaneRightSide(){
        messagePane.setTranslateX(160);
    }

}
