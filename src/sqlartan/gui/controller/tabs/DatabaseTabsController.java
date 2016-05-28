package sqlartan.gui.controller.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.gui.controller.tabs.model.DatabaseStructureModel;
import java.io.IOException;
import static sqlartan.gui.util.ActionButtons.actionButton;

/**
 * Controller of DatabaseTabs.fxml. Controller of the tabs of a database.
 */
public class DatabaseTabsController extends TabsController {

	@FXML
	private TableColumn<DatabaseStructureModel, Number> colLignes;
	@FXML
	private TableColumn<DatabaseStructureModel, String> colRename;
	@FXML
	private TableColumn<DatabaseStructureModel, String> colDelete;
	@FXML
	private TableView<DatabaseStructureModel> structureTable;

	private Database database;

	private ObservableList<DatabaseStructureModel> dbStructs = FXCollections.observableArrayList();


	/**
	 * Add button rename and drop to structure tab
	 *
	 * @throws IOException
	 */
	@FXML
	protected void initialize() throws IOException {
		super.initialize();

		colLignes.setCellValueFactory(param -> param.getValue().lines);

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			DatabaseStructureModel dbStruct = self.getTableView().getItems().get(self.getIndex());
			database.structure(dbStruct.name.get()).ifPresent(Sqlartan.getInstance().getController()::renameStructure);
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			DatabaseStructureModel dbStruct = self.getTableView().getItems().get(self.getIndex());
			database.structure(dbStruct.name.get()).ifPresent(Sqlartan.getInstance().getController()::dropStructure);
		}));

		tabPane.getSelectionModel().clearSelection();
	}


	/**
	 * Display the structure of the database
	 */
	protected void displayStructure() {
		dbStructs.clear();
		dbStructs.addAll(database.structures()
		                         .sorted((a, b) -> a.name().compareTo(b.name()))
		                         .map(DatabaseStructureModel::new)
		                         .toList());
		structureTable.setItems(dbStructs);
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh(Tab selected) {
		if (selected == structureTab) {
			displayStructure();
		}
	}


	/**
	 * Set the database which will be used in this tab
	 *
	 * @param database the database to work on
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
}
