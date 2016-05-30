package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import sqlartan.core.Database;
import sqlartan.gui.controller.SqlartanController;

/**
 * A custom TreeItem with a contextual right click menu
 */
public abstract class CustomTreeItem extends TreeItem {

	protected final SqlartanController controller;
	private final String name;
	protected Database database;

	public CustomTreeItem(String name, SqlartanController controller, Database database) {
		this.name = name;
		this.controller = controller;
		this.database = database;
	}

	/**
	 * Return the contextual menu.
	 *
	 * @return the contextMenu
	 */
	public abstract ContextMenu getMenu();

	/**
	 * Return the type of the treeItem
	 *
	 * @return the type of the TreeItem
	 */
	public abstract Type type();

	/**
	 * Return the database used by the TreeItem
	 *
	 * @return the database used by the TreeItem
	 */
	public Database database() {
		return database;
	}

	/**
	 * Return the name of the TreeItem
	 *
	 * @return the name of the TreeItem
	 */
	public String name() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}
}
