package sqlartan.view;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import sqlartan.core.Database;

/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController {

	private Database database;

	public void setDB(Database database)
	{
		this.database = database;
	}

	@FXML
	private Tab display;

	private void initialize()  {
		display.setContent();
	}

	}
