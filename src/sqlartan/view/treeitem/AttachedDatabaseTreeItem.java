package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

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

		detach.setOnAction(event -> SqlartanController.getDB().attached(name()).ifPresent(db -> {
			try {
				controller.detachDatabase(db);
			} catch (SQLException e) {
				Popup.error("Detach error", e.getMessage());
			}
		}));
		ContextMenu res = super.getMenu();
		res.getItems().add(detach);
		return res;
	}
}
