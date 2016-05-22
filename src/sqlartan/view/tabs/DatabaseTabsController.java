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
import sqlartan.core.stream.IterableStream;
import sqlartan.view.AllRequestController;
import sqlartan.view.tabs.struct.DatabaseStructure;
import java.io.IOException;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController{

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
	private Tab sqlTab;

	private AllRequestController allRequestControler;

	@FXML
	private TableView<DatabaseStructure> structureTable;

	private Database database;

	private ObservableList<DatabaseStructure> dbStructs = FXCollections.observableArrayList();

	@FXML
	private Pane sqlPane;

	@FXML
	private void initialize() {
		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		try {
			Pane pane = loader.load();
			sqlPane.getChildren().add(pane);

			allRequestControler = loader.getController();
			pane.prefHeightProperty().bind(sqlPane.heightProperty());
			pane.prefWidthProperty().bind(sqlPane.widthProperty());

		} catch (IOException e) {

		}

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colActions.setCellValueFactory(param -> param.getValue().actionsProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		tabPane.getSelectionModel().clearSelection();
	}

	/**
	 * Display the structure of the database
	 */
	private void displayStructure() {
		dbStructs.clear();
		dbStructs.addAll(IterableStream.concat(database.tables(), database.views())
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

	public void selectSqlTab(int index){
		tabPane.getSelectionModel().select(sqlTab);
	}
}
