package sqlartan.gui.controller.treeitem;

import sqlartan.core.Database;
import sqlartan.gui.controller.SqlartanController;

/**
 * Used when the TreeItem is a table, the context menu is like the structure
 * menu.
 */
public class ViewTreeItem extends StructureTreeItem {

	public ViewTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type type() {
		return Type.VIEW;
	}

	public String toString() {
		return "V – " + super.toString();
	}
}
