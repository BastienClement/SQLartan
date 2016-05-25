package sqlartan.view.tabs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Callback;
import sqlartan.core.Database;
import sqlartan.core.Table;
import sqlartan.view.SqlartanController;
import sqlartan.view.tabs.struct.TableStructure;
import sqlartan.view.util.Popup;
import java.io.IOException;


/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends TabsController<TableStructure> {


	@FXML
	private TableColumn<TableStructure, String> colRename;
	@FXML
	private TableColumn<TableStructure, String> colDelete;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColName;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColType;
	@FXML
	private TableColumn<InsertRowStructure, CheckBox> insertNull;
	@FXML
	private TableColumn<InsertRowStructure, TextField> insertColValue;


	@FXML
	protected Tab insertTab;


	@FXML
	private TableView<InsertRowStructure> insertTable;


	private Database database;

	private SqlartanController controller;

	private Table table;

	@FXML
	protected void initialize() throws IOException {

		super.initialize();

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == displayTab) {
				displayTab.setContent(dataTableView.getTableView(structure));
			} else if (newTab == structureTab) {
				displayStructure();
			} else if (newTab == insertTab) {
				displayInsertTab();
			}
		});

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			TableStructure tableStruct = self.getTableView().getItems().get(self.getIndex());
			Popup.input("Rename", "Rename " + tableStruct.nameProperty().get() + " into : ", tableStruct.nameProperty().get()).ifPresent(name -> {
				if (name.length() > 0 && !tableStruct.nameProperty().get().equals(name)) {
					controller.renameColumn(table, tableStruct.nameProperty().get(), name);
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			TableStructure tableStruct = self.getTableView().getItems().get(self.getIndex());
			table.column(tableStruct.nameProperty().get()).ifPresent(sqlartan.core.TableColumn::drop);
			controller.refreshView();
		}));

		insertTable.setEditable(true);

		insertColName.setCellValueFactory(param -> param.getValue().name);
		insertColType.setCellValueFactory(param -> param.getValue().type);

		insertColValue.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InsertRowStructure, TextField>, ObservableValue<TextField>>() {
			@Override
			public ObservableValue<TextField> call(TableColumn.CellDataFeatures<InsertRowStructure, TextField> param) {
				ObservableValue<TextField> tf = new SimpleObjectProperty<>(new TextField());
				param.getValue().value.bindBidirectional(tf.getValue().textProperty());
				return tf;
			}
		});

		insertNull.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<InsertRowStructure, CheckBox>, ObservableValue<CheckBox>>() {
			@Override
			public ObservableValue<CheckBox> call(TableColumn.CellDataFeatures<InsertRowStructure, CheckBox> param) {
				ObservableValue<CheckBox> cb = new SimpleObjectProperty<CheckBox>(new CheckBox());
				param.getValue().nulle.bindBidirectional(cb.getValue().selectedProperty());
				return cb;
			}
		});

		insertColValue.setEditable(true);
	}

	public void refresh() {
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

		switch (tabPane.getSelectionModel().getSelectedIndex()) {
			case 0: {
				displayStructure();
			}
			break;
			case 1: {
				displayTab.setContent(dataTableView.getTableView(structure));
			}
			break;
			case 2: {
				displayInsertTab();
			}
		}


	}

	private void displayInsertTab() {
		ObservableList<InsertRowStructure> insertRows = FXCollections.observableArrayList();

		insertRows.addAll(structure.columns().map(InsertRowStructure::new).toList());

		insertTable.setItems(insertRows);

	}

	@FXML
	private void submitNewData() {
		ObservableList<InsertRowStructure> insertRows = insertTable.getItems();

		int a = 3;


		//TODO call the insert methode on the core
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
	 * Set the table
	 *
	 * @param table
	 */
	public void setTable(Table table) {
		this.table = table;
	}
}
