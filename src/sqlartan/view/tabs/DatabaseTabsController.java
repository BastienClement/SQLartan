package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.view.AllRequestController;
import sqlartan.view.SqlartanController;
import sqlartan.view.tabs.struct.DatabaseStructure;
import sqlartan.view.util.Popup;
import java.io.IOException;
import java.util.function.BiConsumer;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController {

	@FXML
	private TableColumn<DatabaseStructure, String> colName;
	@FXML
	private TableColumn<DatabaseStructure, String> colType;
	@FXML
	private TableColumn<DatabaseStructure, String> colLignes;
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

	private SqlartanController controller;

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
		} catch (IOException ignored) {}

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		colDelete.setCellFactory(actionButton("Rename", (self, event) -> {
			DatabaseStructure dbStruct = self.getTableView().getItems().get(self.getIndex());
			String structName = dbStruct.nameProperty().get();
			Popup.input("Rename", "Rename " + structName + " into : ", structName).ifPresent(name -> {
				if (name.length() > 0 && !structName.equals(name)) {
					database.structure(structName).ifPresent(s -> controller.renameStructure(s, name));
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			DatabaseStructure dbStruct = self.getTableView().getItems().get(self.getIndex());
			database.structure(dbStruct.nameProperty().get()).ifPresent(s -> controller.dropStructure(s));
		}));

		tabPane.getSelectionModel().clearSelection();
	}

	private Callback<TableColumn<DatabaseStructure, String>, TableCell<DatabaseStructure, String>>
			actionButton(String label, BiConsumer<TableCell<DatabaseStructure, String>, ActionEvent> action) {
		Button btn = new Button(label);
		return param -> new TableCell<DatabaseStructure, String>() {
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
					setText(null);
				} else {
					btn.setOnAction(event -> action.accept(this, event));
					setGraphic(btn);
					setText(null);
				}
			}
		};
	}

	/**
	 * Display the structure of the database
	 */
	private void displayStructure() {
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

	/**
	 * Set the controller
	 *
	 * @param controller
	 */
	public void setController(SqlartanController controller) {
		this.controller = controller;
	}

	public void selectSqlTab() {
		tabPane.getSelectionModel().selectFirst();
		tabPane.getSelectionModel().selectNext();
	}

	public void setSqlRequest(String request) {
		allRequestControler.setRequest(request);
	}
}
