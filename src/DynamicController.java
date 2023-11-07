import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.io.IOException;

public class DynamicController {

    @FXML
    private Label label; // Example component in the dynamic pane

    @FXML
    public void initialize() throws IOException {

    }

    // Method to set data in the dynamic pane components
    public void setData(String data) {
        label.setText(data);
    }
}
