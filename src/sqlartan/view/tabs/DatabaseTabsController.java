package sqlartan.view.tabs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController extends TabsController {

	private PersistentStructure<?> structure;

	@FXML
	private Pane sqlTab;

	@FXML
	private void initialize() {
		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);

	}


}
