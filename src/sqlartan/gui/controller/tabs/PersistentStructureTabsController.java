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
 * Abstract class to factorize the code for the table and view controllers.
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
	 * <p>
	 * Add column to the structure tab.
	 */
	protected void initialize() throws IOException {
		super.initialize();

		colNo.setCellValueFactory(param -> param.getValue().no);
		colNull.setCellValueFactory(param -> param.getValue().nullable);
	}

	/**
	 * Display the structure, if the structure can't be displayed, a popup will
	 * ask the user if he want to drop it.
	 */
	@Override
	protected void displayStructure() {
		ObservableList<PersistentStructureModel> tableStructures = FXCollections.observableArrayList();
		displaySafely(() -> {
			int[] i = { 0 };
			tableStructures.addAll(structure.columns()
			                                .map(c -> new PersistentStructureModel(c, ++i[0]))
			                                .toList());
		});
		structureTable.setItems(tableStructures);
	}

	/**
	 * Display the data table
	 */
	protected void displayData() {
		displaySafely(() -> displayTab.setContent(DataTableView.getTableView(structure.selectAll())));
	}

	/**
	 * Display the stuff safely.
	 * If something went wrong, ask the user if he wants to drop the structure.
	 *
	 * @param stuff the stuff to do
	 */
	private void displaySafely(Runnable stuff) {
		try {
			stuff.run();
		} catch (UncheckedSQLException e) {
			Platform.runLater(() -> {
				ButtonType ok = new ButtonType("Yes drop it");
				ButtonType cancel = new ButtonType("Cancel");
				Popup.warning("Error while displaying " + structure.name(), e.getMessage(), ok, cancel)
				     .filter(d -> d == ok)
				     .ifPresent(d -> {
					     structure.drop();
					     Sqlartan.getInstance().getController().refreshView();
				     });
				Sqlartan.getInstance().getController().selectTreeIndex(0);
			});
		}
	}

	/**
	 * Set the structure to use for the structure tab.
	 *
	 * @param structure the structure
	 */
	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}
}