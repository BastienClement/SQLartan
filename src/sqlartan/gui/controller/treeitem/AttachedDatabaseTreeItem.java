package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.gui.controller.SqlartanController;
import sqlartan.core.Database;

/**
 * Created by Adriano on 04.05.2016.
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

		detach.setOnAction(event -> controller.database().detach(database.name()));
		ContextMenu res = super.getMenu();
		res.getItems().add(detach);
		return res;
	}
}
