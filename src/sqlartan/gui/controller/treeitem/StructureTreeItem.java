package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import sqlartan.gui.controller.SqlartanController;

/**
 * Used when the TreeItem is a database, the context menu is
 * - Drop
 * - Duplicate
 * - Rename
 */
public abstract class StructureTreeItem extends CustomTreeItem {

	protected PersistentStructure<?> structure;

	public StructureTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
		database.structure(name).ifPresent(s -> structure = s);

		if (structure == null)
			throw new RuntimeException("The structure " + name + "doesn't exist in the database" + database.name());
	}

	/**
	 * Add drop, rename and duplicate menu.
	 * {@inheritDoc}
	 */
	@Override
	public ContextMenu getMenu() {

		MenuItem drop = new MenuItem("Drop");
		MenuItem rename = new MenuItem("Rename");
		MenuItem duplicate = new MenuItem("Duplicate");


		drop.setOnAction(event -> controller.dropStructure(structure));
		rename.setOnAction(event -> controller.renameStructure(structure));
		duplicate.setOnAction(event -> controller.duplicate(structure));

		return new ContextMenu(drop, duplicate, rename);
	}
}
