package sqlartan.view.tabs;

import javafx.scene.control.Tab;

/**
 * Projet : SQLartan
 * Créé le 24.05.16.
 * @author Adriano Ruberto
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
