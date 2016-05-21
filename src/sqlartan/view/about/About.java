package sqlartan.view.about;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Pane;

/**
 * Created by julien on 20.05.16.
 */
public class About {

	@FXML
	private TextArea description;

	@FXML
	private Pane mainPane;

	@FXML
	private void initialize() {
		description.setFocusTraversable(false);
		description.setMouseTransparent(true);
	}
}
