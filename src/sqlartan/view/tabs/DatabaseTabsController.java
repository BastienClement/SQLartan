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

		} catch (IOException e) {

		}

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());
		colRename.setCellFactory(new Callback<TableColumn<DatabaseStructure, String>, TableCell<DatabaseStructure, String>>() {
			@Override
			public TableCell<DatabaseStructure, String> call(final TableColumn<DatabaseStructure, String> param) {
				return new TableCell<DatabaseStructure, String>() {
					private final Button btn = new Button("Rename");

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							btn.setOnAction((ActionEvent event) ->
							{
								DatabaseStructure dbStruct = getTableView().getItems().get(getIndex());
								String oldName = dbStruct.nameProperty().get();
								Popup.input("Rename", "Rename " + oldName + " into : ", oldName).ifPresent(newName -> {
									if (newName.length() > 0 && !oldName.equals(newName)) {
										database.structure(oldName)
										        .ifPresent(s -> controller.renameColumn(s, oldName, newName));
									}
								});
							});
							setGraphic(btn);
							setText(null);
						}
					}
				};
			}
		});
		colDelete.setCellFactory(new Callback<TableColumn<DatabaseStructure, String>, TableCell<DatabaseStructure, String>>() {
			@Override
			public TableCell<DatabaseStructure, String> call(final TableColumn<DatabaseStructure, String> param) {
				return new TableCell<DatabaseStructure, String>() {
					private final Button btn = new Button("Drop");

					@Override
					public void updateItem(String item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setGraphic(null);
							setText(null);
						} else {
							btn.setOnAction((ActionEvent event) ->
							{
								DatabaseStructure dbStruct = getTableView().getItems().get(getIndex());
								database.structure(dbStruct.nameProperty().get())
								        .ifPresent(structure -> controller.dropStructure(structure));
							});
							setGraphic(btn);
							setText(null);
						}
					}
				};
			}
		});

		tabPane.getSelectionModel().clearSelection();
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
