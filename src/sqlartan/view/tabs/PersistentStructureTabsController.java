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
import sqlartan.view.tabs.structureTab.TableStructureTab;
import sqlartan.view.util.Popup;

/**
 * Projet : SQLartan
 * Créé le 26.05.2016.
 *
 * @author Adriano Ruberto
 */
public abstract class PersistentStructureTabsController extends TabsController<TableStructureTab> {

	@FXML
	protected Tab displayTab;

	protected PersistentStructure<?> structure;


	@FXML
	protected TableColumn<TableStructureTab, String> colNull;
	@FXML
	protected TableColumn<TableStructureTab, String> colDefaultValue;
	@FXML
	protected TableColumn<TableStructureTab, String> colComment;

	/**
	 * {@inheritDoc}
	 */
	protected void displayStructure() {
		ObservableList<TableStructureTab> tableStructures = FXCollections.observableArrayList();

		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());


		TableStructureTab.IDReset();
		try {
			tableStructures.addAll(structure.columns()
			                                .map(TableStructureTab::new)
			                                .toList());
		} catch (UncheckedSQLException e) {
			Platform.runLater(() -> {
				ButtonType ok = new ButtonType("Yes drop it");
				ButtonType cancel = new ButtonType("Cancel");
				Popup.warning("Error while display structure", e.getMessage(), ok, cancel)
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
	protected void displayDataTable() {
		displayTab.setContent(dataTableView.getTableView(structure));
	}

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}
}
