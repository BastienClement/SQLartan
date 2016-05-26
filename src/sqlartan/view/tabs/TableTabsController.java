package sqlartan.view.tabs;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sqlartan.Sqlartan;
import sqlartan.core.InsertRow;
import sqlartan.core.Table;
import sqlartan.view.tabs.structureTab.TableStructureTab;
import sqlartan.view.util.Popup;
import java.io.IOException;
import java.sql.SQLException;
import static sqlartan.view.util.ActionButtons.actionButton;


/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends PersistentStructureTabsController {

	@FXML
	protected Tab insertTab;
	@FXML
	private TableColumn<TableStructureTab, String> colRename;
	@FXML
	private TableColumn<TableStructureTab, String> colDelete;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColName;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColType;
	@FXML
	private TableColumn<InsertRowStructure, CheckBox> insertNull;
	@FXML
	private TableColumn<InsertRowStructure, TextField> insertColValue;
	@FXML
	private TableView<InsertRowStructure> insertTable;

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
			TableStructureTab tableStruct = self.getTableView().getItems().get(self.getIndex());
			Popup.input("Rename", "Rename " + tableStruct.nameProperty().get() + " into : ", tableStruct.nameProperty().get()).ifPresent(name -> {
				if (name.length() > 0 && !tableStruct.nameProperty().get().equals(name)) {
					Sqlartan.getInstance().getController().renameColumn((Table) structure, tableStruct.nameProperty().get(), name);
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			TableStructureTab tableStruct = self.getTableView().getItems().get(self.getIndex());
			((Table) structure).column(tableStruct.nameProperty().get()).ifPresent(sqlartan.core.TableColumn::drop);
			Sqlartan.getInstance().getController().refreshView();
		}));

		insertTable.setEditable(true);

		insertColName.setCellValueFactory(param -> param.getValue().nameProperty());
		insertColType.setCellValueFactory(param -> param.getValue().typeProperty());

		insertColValue.setCellValueFactory(param -> {
			ObservableValue<TextField> tf = new SimpleObjectProperty<>(new TextField());
			param.getValue().valueProperty().bindBidirectional(tf.getValue().textProperty());
			return tf;
		});

		insertNull.setCellValueFactory(param -> {
			ObservableValue<CheckBox> cb = new SimpleObjectProperty<>(new CheckBox());
			param.getValue().nullableProperty().bindBidirectional(cb.getValue().selectedProperty());
			return cb;
		});

		insertColValue.setEditable(true);
	}


	/**
	 * TODO
	 */
	public void refresh() {
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

	/**
	 * TODO
	 */
	private void displayInsertTab() {
		ObservableList<InsertRowStructure> insertRows = FXCollections.observableArrayList();

		insertRows.addAll(structure.columns().map(InsertRowStructure::new).toList());

		insertTable.setItems(insertRows);
	}


	/**
	 * TODO
	 *
	 * @throws SQLException
	 */
	@FXML
	private void submitNewData() throws SQLException {
		try {
			Object objects[] = InsertRowStructure.toArray(insertTable.getItems());
			InsertRow insertRow = ((Table) structure).insert();

			insertRow.set(objects);

			insertRow.execute();

		} catch (Exception e) {
			Popup.error("Error while inserting data", e.getMessage());
		}
	}
}
