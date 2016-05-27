package sqlartan.view.tabs;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.Sqlartan;
import sqlartan.core.Column;
import sqlartan.core.PersistentStructure;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.view.util.DataTableView;
import sqlartan.view.util.Popup;
import java.io.IOException;

/**
 * Projet : SQLartan
 * Créé le 26.05.2016.
 *
 * @author Adriano Ruberto
 */
public abstract class PersistentStructureTabsController extends TabsController {
	@FXML
	protected Tab displayTab;
	protected PersistentStructure<?> structure;
	@FXML
	protected TableView<PersistentStructureTab> structureTable;
	@FXML
	protected TableColumn<PersistentStructureTab, Number> colNo;
	@FXML
	protected TableColumn<PersistentStructureTab, String> colNull;
	@FXML
	protected TableColumn<PersistentStructureTab, String> colDefaultValue;
	@FXML
	protected TableColumn<PersistentStructureTab, String> colComment;
	protected void initialize() throws IOException {
		super.initialize();

		colComment.setCellValueFactory(param -> param.getValue().comment);
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValue);
		colNo.setCellValueFactory(param -> param.getValue().no);
		colNull.setCellValueFactory(param -> param.getValue().nullable);
	}
	/**
	 * {@inheritDoc}
	 */
	protected void displayStructure() {
		ObservableList<PersistentStructureTab> tableStructures = FXCollections.observableArrayList();

		try {
			int[] i = { 0 };
			tableStructures.addAll(structure.columns()
			                                .map(c -> new PersistentStructureTab(c, ++i[0]))
			                                .toList());
		} catch (UncheckedSQLException e) {
			Platform.runLater(() -> {
				ButtonType ok = new ButtonType("Yes drop it");
				ButtonType cancel = new ButtonType("Cancel");
				Popup.warning("Error while displaying " + structure.name(), e.getMessage(), ok, cancel)
				     .filter(d -> d == ok)
				     .ifPresent(d -> Sqlartan.getInstance().getController().dropStructure(structure));
				Sqlartan.getInstance().getController().selectTreeIndex(0);
			});
		}

		structureTable.setItems(tableStructures);
	}
	/**
	 * Display the data table
	 */
	protected void displayData() {
		displayTab.setContent(DataTableView.getTableView(structure.selectAll()));
	}
	/**
	 * TODO
	 *
	 * @param structure
	 */
	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

	/**
	 * Represent the structure tab for a view or a table
	 */
	protected class PersistentStructureTab extends TabsController.StructureTab {
		private final IntegerProperty no;
		private final StringProperty nullable;
		private final StringProperty defaultValue;
		private final StringProperty comment;

		private PersistentStructureTab(Column column, int ID) {
			super(column.name(), column.type());
			this.no = new SimpleIntegerProperty(ID);
			this.nullable = new SimpleStringProperty(column.nullable() ? "True" : "False");
			this.defaultValue = new SimpleStringProperty(); // TODO
			this.comment = new SimpleStringProperty(); // TODO
		}
	}
}
