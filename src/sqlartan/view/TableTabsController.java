package sqlartan.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import java.io.IOException;

/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController {

	private Database database;

	private TableVue tableVue = new TableVue();

	@FXML
	private Tab displayTab;

	@FXML
	private Tab sqlTab;

	public void setDB(Database database)
	{
		this.database = database;
	}

	@FXML
	private void initialize()
	{
		FXMLLoader allRequestLoader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		try {
			Pane allRequestPane = allRequestLoader.load();
			sqlTab.setContent(allRequestPane);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}


	public void init(PersistentStructure<?> structure)
	{
		displayTab.setContent(tableVue.getTableView(structure));
	}

}
