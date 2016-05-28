package sqlartan.gui.controller.tabs;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.gui.controller.tabs.model.PersistentStructureModel;
import sqlartan.gui.util.DataTableView;
import sqlartan.gui.util.Popup;
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
	protected TableView<PersistentStructureModel> structureTable;
	@FXML
	protected TableColumn<PersistentStructureModel, Number> colNo;
	@FXML
	protected TableColumn<PersistentStructureModel, String> colNull;

	/**
	 * {@inheritDoc}
	 *
	 * Add column to the structure tab
	 */
	protected void initialize() throws IOException {
		super.initialize();

		colNo.setCellValueFactory(param -> param.getValue().no);
		colNull.setCellValueFactory(param -> param.getValue().nullable);
	}
	/**
	 * Display the structure, if the structure can't be displayed, a popup will ask the user if he want to drop it.
	 */
	@Override
	protected void displayStructure() {
		ObservableList<PersistentStructureModel> tableStructures = FXCollections.observableArrayList();

		try {
			int[] i = { 0 };
			tableStructures.addAll(structure.columns()
			                                .map(c -> new PersistentStructureModel(c, ++i[0]))
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
	 * Set the structure to use for the structure tab
	 *
	 * @param structure the structure
	 */
	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

}
