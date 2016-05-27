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
import sqlartan.view.tabs.model.InsertRowModel;
import sqlartan.view.tabs.model.StructureModel;
import sqlartan.view.tabs.model.PersistentStructureModel;
import sqlartan.view.util.Popup;
import java.io.IOException;
import java.sql.SQLException;
import static sqlartan.view.util.ActionButtons.actionButton;


/**
 * TODO JAVADOC
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends PersistentStructureTabsController {

	@FXML
	protected Tab insertTab;
	@FXML
	private TableColumn<PersistentStructureModel, String> colRename;
	@FXML
	private TableColumn<PersistentStructureModel, String> colDelete;
	@FXML
	private TableColumn<InsertRowModel, String> insertColName;
	@FXML
	private TableColumn<InsertRowModel, String> insertColType;
	@FXML
	private TableColumn<InsertRowModel, CheckBox> insertNull;
	@FXML
	private TableColumn<InsertRowModel, TextField> insertColValue;
	@FXML
	private TableView<InsertRowModel> insertTable;

	private ObservableList<InsertRowModel> insertRows = FXCollections.observableArrayList();

	/**
	 * {@inheritDoc}
	 * Create the button rename and drop, complete the structure tab
	 */
	@FXML
	protected void initialize() throws IOException {
		super.initialize();

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			StructureModel tableStruct = self.getTableView().getItems().get(self.getIndex());
			Popup.input("Rename", "Rename " + tableStruct.name.get() + " into : ", tableStruct.name.get()).ifPresent(name -> {
				if (name.length() > 0 && !tableStruct.name.get().equals(name)) {
					Sqlartan.getInstance().getController().renameColumn((Table) structure, tableStruct.name.get(), name);
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			StructureModel tableStruct = self.getTableView().getItems().get(self.getIndex());
			((Table) structure).column(tableStruct.name.get()).ifPresent(sqlartan.core.TableColumn::drop);
			Sqlartan.getInstance().getController().refreshView();
		}));

		insertTable.setEditable(true); // TODO test false

		insertColName.setCellValueFactory(param -> param.getValue().name);
		insertColType.setCellValueFactory(param -> param.getValue().type);

		insertColValue.setCellValueFactory(param -> {
			ObservableValue<TextField> tf = new SimpleObjectProperty<>(new TextField());
			param.getValue().value.bindBidirectional(tf.getValue().textProperty());
			return tf;
		});

		insertNull.setCellValueFactory(param -> {
			ObservableValue<CheckBox> cb = new SimpleObjectProperty<>(new CheckBox());
			param.getValue().nullable.bindBidirectional(cb.getValue().selectedProperty());
			return cb;
		});

		insertColValue.setEditable(true);
	}


	/**
	 * Display the insert tab
	 */
	private void displayInsertTab() {
		insertRows.clear();
		insertRows.addAll(structure.columns().map(InsertRowModel::new).toList());
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
			Object objects[] = InsertRowModel.toArray(insertTable.getItems());
			InsertRow insertRow = ((Table) structure).insert();

			insertRow.set(objects);

			insertRow.execute();

		} catch (Exception e) {
			Popup.error("Error while inserting data", e.getMessage());
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh(Tab selected) {
		if (selected == structureTab) {
			displayStructure();
		} else if (selected == displayTab) {
			displayData();
		} else if (selected == insertTab) {
			displayInsertTab();
		}
	}

}
