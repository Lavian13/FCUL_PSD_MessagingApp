import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class MessageController {

    @FXML
    private Label label; // Example component in the dynamic pane

    @FXML
    public void initialize() throws IOException {

    }

    public void setData(String data) {
        label.setText(data);
    }

}
