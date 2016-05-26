package sqlartan.view.tabs;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import sqlartan.Sqlartan;
import sqlartan.core.Column;
import sqlartan.core.InsertRow;
import sqlartan.core.Table;
import sqlartan.core.Type;
import sqlartan.view.util.Popup;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import static sqlartan.view.util.ActionButtons.actionButton;


/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends PersistentStructureTabsController {

	@FXML
	protected Tab insertTab;
	@FXML
	private TableColumn<PersistentStructureTab, String> colRename;
	@FXML
	private TableColumn<PersistentStructureTab, String> colDelete;
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
				displayData();
			} else if (newTab == structureTab) {
				displayStructure();
			} else if (newTab == insertTab) {
				displayInsertTab();
			}
		});

		colRename.setCellFactory(actionButton("Rename", (self, event) -> {
			StructureTab tableStruct = self.getTableView().getItems().get(self.getIndex());
			Popup.input("Rename", "Rename " + tableStruct.name.get() + " into : ", tableStruct.name.get()).ifPresent(name -> {
				if (name.length() > 0 && !tableStruct.name.get().equals(name)) {
					Sqlartan.getInstance().getController().renameColumn((Table) structure, tableStruct.name.get(), name);
				}
			});
		}));

		colDelete.setCellFactory(actionButton("Drop", (self, event) -> {
			StructureTab tableStruct = self.getTableView().getItems().get(self.getIndex());
			((Table) structure).column(tableStruct.name.get()).ifPresent(sqlartan.core.TableColumn::drop);
			Sqlartan.getInstance().getController().refreshView();
		}));

		insertTable.setEditable(true);

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
	 * TODO
	 */
	public void refresh() {
		switch (tabPane.getSelectionModel().getSelectedIndex()) {
			case 0: {
				displayStructure();
			}
			break;
			case 1: {
				displayData();
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
	/**
	 * Created by julien on 21.05.16.
	 */
	private static class InsertRowStructure {
		private final StringProperty name;
		private final StringProperty type;
		private final StringProperty value;
		private final BooleanProperty nullable;
		private final Type typed;

		private InsertRowStructure(Column column) {
			name = new SimpleStringProperty(column.name());
			type = new SimpleStringProperty(column.type());
			value = new SimpleStringProperty();
			nullable = new SimpleBooleanProperty();
			typed = column.affinity().type;

			nullable.addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					value.setValue(null);
				}
			});

			value.addListener((observable, oldValue, newValue) -> {
				if (newValue != null) {
					nullable.setValue(false);
				}
			});
		}
		/**
		 * Make an object table with the good typs for the sql insertion
		 *
		 * @param liste
		 * @return the object table
		 * @throws Exception
		 */
		private static Object[] toArray(ObservableList<InsertRowStructure> liste) throws Exception {
			List<Object> lk = new LinkedList<>();

			for (InsertRowStructure irs : liste) {
				Object obj;

				switch (irs.typed) {
					case Integer:
						obj = new Integer(irs.value.getValue());
						break;
					case Real:
						obj = new Double(irs.value.getValue());
						break;
					default:
						obj = irs.value.getValue();
						break;
				}

				lk.add(obj);
			}
			return lk.toArray();
		}
	}
}
