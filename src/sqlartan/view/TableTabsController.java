package sqlartan.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;

/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends TabsController {

	private PersistentStructure<?> structure;

	private TableVue tableVue = new TableVue();

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab displayTab;

	@FXML
	private Pane sqlTab;

	@FXML
	private TableView structureTab;

	@FXML
	private void initialize() {
		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);

		/**
		 * Display the datas from the table in display tab only when he's active.
		 * Every time a new query is done.
		 */
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == displayTab) {
				displayTab.setContent(tableVue.getTableView(structure));
			}
		});
	}

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

}
