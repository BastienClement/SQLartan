package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
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
public class TableTabsController extends TabsController {


	@FXML
	private TableColumn<TableStructure, String> colRename;
	@FXML
	private TableColumn<TableStructure, String> colDelete;

	@FXML
	private TableColumn<InsertRowStructure, String> insertColName;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColType;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColValue;




	@FXML
	protected Tab insertTab;





	@FXML
	private TableView<InsertRowStructure> insertTable;


	private Database database;

	private SqlartanController controller;

	private Table table;

	@FXML
	protected void initialize() throws  IOException{

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




		colRename.setCellFactory(new Callback<TableColumn<TableStructure, String>, TableCell<TableStructure, String>>() {
			@Override
			public TableCell<TableStructure, String> call(final TableColumn<TableStructure, String> param) {
				return new TableCell<TableStructure, String>() {
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
								TableStructure tableStruct = getTableView().getItems().get(getIndex());
								Popup.input("Rename", "Rename " + tableStruct.nameProperty().get() + " into : ", tableStruct.nameProperty().get()).ifPresent(name -> {
									if (name.length() > 0 && !tableStruct.nameProperty().get().equals(name)) {
										controller.renameColumn(table, tableStruct.nameProperty().get(), name);
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

		colDelete.setCellFactory(new Callback<TableColumn<TableStructure, String>, TableCell<TableStructure, String>>() {
			@Override
			public TableCell<TableStructure, String> call(final TableColumn<TableStructure, String> param) {
				return new TableCell<TableStructure, String>() {
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
								TableStructure tableStruct = getTableView().getItems().get(getIndex());
								table.column(tableStruct.nameProperty().get()).ifPresent(sqlartan.core.TableColumn::drop);
								controller.refreshView();
							});
							setGraphic(btn);
							setText(null);
						}
					}
				};
			}
		});

		insertTable.setEditable(true);

		insertColName.setCellValueFactory(param -> param.getValue().name);
		insertColType.setCellValueFactory(param -> param.getValue().type);
		insertColValue.setCellValueFactory(param -> param.getValue().value);

		insertColValue.setCellFactory(TextFieldTableCell.forTableColumn()); //TODO eventuelement modifier pour toujour aficher le textfield

		insertColValue.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(event.getNewValue());
		});

		insertColValue.setEditable(true);
	}

	public void refresh() {
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();

		switch (tabPane.getSelectionModel().getSelectedIndex()){
			case 0:{
				displayStructure();
			}
			break;
			case 1:
			{
				displayTab.setContent(dataTableView.getTableView(structure));
			}
			break;
			case 2:{
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
	 * Set the controller
	 *
	 * @param controller
	 */
	public void setController(SqlartanController controller) {
		this.controller = controller;
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
