package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.AttachedDatabase;
import sqlartan.core.Database;
import sqlartan.gui.controller.SqlartanController;

/**
 * Used when the TreeItem is a Attached database
 */
public class AttachedDatabaseTreeItem extends DatabaseTreeItem {

	public AttachedDatabaseTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
	}
	/**
	 * {@inheritDoc}
	 * Add a detach menu
	 */
	@Override
	public ContextMenu getMenu() {
		MenuItem detach = new MenuItem("Detach");

		detach.setOnAction(event -> controller.detachDatabase((AttachedDatabase) database));
		ContextMenu res = super.getMenu();
		res.getItems().add(detach);
		return res;
	}
}
