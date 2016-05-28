package sqlartan.gui.controller.tabs;

import javafx.scene.control.Tab;

/**
 * Controller of ViewTabs.fxml. Controller of the tabs of a view
 */
public class ViewTabsController extends PersistentStructureTabsController {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void refresh(Tab selected) {
		if (selected == structureTab) {
			displayStructure();
		} else if (selected == displayTab) {
			displayData();
		}
	}
}
