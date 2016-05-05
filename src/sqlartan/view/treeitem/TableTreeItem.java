package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;

public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");

		truncate.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(table -> {
			controller.truncateTable(table);
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
