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
	 * @return a contextMenu
	 */
	public abstract ContextMenu getMenu();

	/**
	 * @return the type of the treeItem
	 */
	public abstract Type type();

	/**
	 * @return the database used by the TreeItem
	 */
	public Database database() {
		return database;
	}

	/**
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
