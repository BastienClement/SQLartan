package sqlartan.view.tabs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.Column;
import sqlartan.core.PersistentStructure;
import sqlartan.core.stream.IterableStream;
import sqlartan.view.DataTableView;
import java.util.Optional;


/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends TabsController {

	private PersistentStructure<?> structure;

	private DataTableView dataTableView = new DataTableView();

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab displayTab;

	@FXML
	private Tab structureTab;

	@FXML
	private Pane sqlTab;

	@FXML
	private TableView structureTable;

	@FXML
	private void initialize() {
		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);

		/**
		 * Display the datas from the table in display tab only when he's active.
		 * Every time a new query is done.
		 */
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
				if (newTab == displayTab) {
					displayTab.setContent(dataTableView.getTableView(structure));
				} else if (newTab == structureTab) {
					displayStructure();
				}
		});
	}

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

	private void displayStructure()
	{


	}

}
