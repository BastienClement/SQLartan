package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;

/**
 * Created by Adriano on 04.05.2016.
 */
public class AttachedDatabaseTreeItem extends DatabaseTreeItem {
	public AttachedDatabaseTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem detach = new MenuItem("Detach");

		detach.setOnAction(event -> SqlartanController.getDB()
		                                              .attached(name())
		                                              .ifPresent(db -> controller.detachDatabase(db)));
		ContextMenu res = super.getMenu();
		res.getItems().add(detach);
		return res;
	}
}
