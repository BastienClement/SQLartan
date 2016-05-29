package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.Database;
import sqlartan.core.Table;
import sqlartan.gui.controller.SqlartanController;

/**
 * Used when the TreeItem is a table, the context menu is like the structure menu but add
 * - Add column
 * - Truncate
 */
public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
	}


	/** {@inheritDoc} */
	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");
		MenuItem addColumn = new MenuItem("Add column");

		truncate.setOnAction(event -> controller.truncate((Table) structure));
		addColumn.setOnAction(event -> controller.addColumn((Table) structure));

		ContextMenu res = super.getMenu();
		res.getItems().addAll(addColumn, truncate);
		return res;
	}


	/** {@inheritDoc} */
	@Override
	public Type type() {
		return Type.TABLE;
	}

	public String toString() {
		return "T – " + super.toString();
	}
}
