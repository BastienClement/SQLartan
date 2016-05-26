package sqlartan.view.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.view.DataTableView;
import sqlartan.view.tabs.structureTab.TableStructureTab;
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
	protected TableColumn<TableStructureTab, Number> colNo;
	@FXML
	protected TableColumn<TableStructureTab, String> colNull;
	@FXML
	protected TableColumn<TableStructureTab, String> colDefaultValue;
	@FXML
	protected TableColumn<TableStructureTab, String> colComment;
	private DataTableView dataTableView = new DataTableView();


	protected void initialize() throws IOException {
		super.initialize();

		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
	}
	/**
	 * {@inheritDoc}
	 */
	protected void displayStructure() {
		ObservableList<TableStructureTab> tableStructures = FXCollections.observableArrayList();

		try {
			int[] i = { 0 };
			tableStructures.addAll(structure.columns()
			                                .map(c -> new TableStructureTab(c, ++i[0]))
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
		dataTableView = new DataTableView();
		displayTab.setContent(dataTableView.getTableView(structure));
	}

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}
}
