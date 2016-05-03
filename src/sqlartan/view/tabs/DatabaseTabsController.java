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
import sqlartan.view.tabs.struct.DatabaseStructure;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController extends TabsController {

	@FXML
	private TableColumn<DatabaseStructure, String> colName;
	@FXML
	private TableColumn<DatabaseStructure, String> colType;
	@FXML
	private TableColumn<DatabaseStructure, String> colLignes;
	@FXML
	private TableColumn<DatabaseStructure, String> colActions;
	@FXML
	private TabPane tabPane;

	@FXML
	private Tab structureTab;

	@FXML
	private TableView<DatabaseStructure> structureTable;

	private Database database;

	private ObservableList<DatabaseStructure> dbStructs = FXCollections.observableArrayList();

	@FXML
	private Pane sqlTab;

	@FXML
	private void initialize() {
		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});
	}

	/**
	 * Display the structure of the database
	 */
	private void displayStructure() {
		dbStructs.clear();
		dbStructs.addAll(Stream.concat(database.tables(), database.views())
		                       .sorted((a, b) -> a.name().compareTo(b.name()))
		                       .map(DatabaseStructure::new)
		                       .collect(Collectors.toList()));


		structureTable.setItems(dbStructs);

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colActions.setCellValueFactory(param -> param.getValue().actionsProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());
	}

	/**
	 * Set the database
	 * @param database
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
}
