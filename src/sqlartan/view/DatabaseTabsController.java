package sqlartan.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import java.io.IOException;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController {

	private PersistentStructure<?> structure;

	@FXML
	private Pane sqlTab;

	@FXML
	private void initialize()
	{
		FXMLLoader allRequestLoader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		try {
			Pane allRequestPane = allRequestLoader.load();

			sqlTab.getChildren().add(allRequestPane);

			allRequestPane.prefHeightProperty().bind(sqlTab.heightProperty());
			allRequestPane.prefWidthProperty().bind(sqlTab.widthProperty());


		} catch (IOException e) {
			e.printStackTrace();
		}
	}


}
