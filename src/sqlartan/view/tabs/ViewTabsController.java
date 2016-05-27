package sqlartan.view.tabs;

import javafx.scene.control.Tab;

/**
 * Created by julien on 24.05.16.
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
