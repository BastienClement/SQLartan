package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");

		truncate.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(table -> {
			try {
				controller.truncateTable(table);
			} catch (SQLException e) {
				Popup.error("Truncate error", e.getMessage());
			}
		}));

		ContextMenu res = super.getMenu();
		res.getItems().add(truncate);
		return res;
	}
	@Override
	public Type type() {
		return Type.TABLE;
	}
}
