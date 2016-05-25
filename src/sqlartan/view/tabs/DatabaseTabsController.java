package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.view.AllRequestController;
import sqlartan.view.tabs.struct.DatabaseStructure;
import sqlartan.view.util.Popup;
import java.io.IOException;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController extends TabsController<DatabaseStructure> {

	@FXML
	private TableColumn<DatabaseStructure, String> colName;
	@FXML
	private TableColumn<DatabaseStructure, String> colType;
	@FXML
	private TableColumn<DatabaseStructure, Number> colLignes;
	@FXML
	private TableColumn<DatabaseStructure, String> colRename;
	@FXML
	private TableColumn<DatabaseStructure, String> colDelete;
	@FXML
	private TabPane tabPane;

	@FXML
	private Tab structureTab;

	@FXML
	private Tab sqlTab;

	private AllRequestController allRequestControler;

	@FXML
	private TableView<DatabaseStructure> structureTable;

	private Database database;

	private ObservableList<DatabaseStructure> dbStructs = FXCollections.observableArrayList();

	@FXML
	private Pane sqlPane;

	@FXML
	protected void initialize() {
		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		try {
			Pane pane = loader.load();
			sqlPane.getChildren().add(pane);

			allRequestControler = loader.getController();
			pane.prefHeightProperty().bind(sqlPane.heightProperty());
			pane.prefWidthProperty().bind(sqlPane.widthProperty());
		} catch (IOException ignored) {}

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			DatabaseStructure dbStruct = self.getTableView().getItems().get(self.getIndex());
			String structName = dbStruct.nameProperty().get();
			Popup.input("Rename", "Rename " + structName + " into : ", structName).ifPresent(name -> {
				if (name.length() > 0 && !structName.equals(name)) {
					database.structure(structName).ifPresent(s -> Sqlartan.getInstance().getController().renameStructure(s, name));
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			DatabaseStructure dbStruct = self.getTableView().getItems().get(self.getIndex());
			database.structure(dbStruct.nameProperty().get()).ifPresent(s -> Sqlartan.getInstance().getController().dropStructure(s));
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
		                         .map(DatabaseStructure::new)
		                         .toList());
		structureTable.setItems(dbStructs);
	}

	/**
	 * Set the database
	 *
	 * @param database
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}


	public void selectSqlTab() {
		tabPane.getSelectionModel().selectFirst();
		tabPane.getSelectionModel().selectNext();
	}

	public void setSqlRequest(String request) {
		allRequestControler.setRequest(request);
	}
}
