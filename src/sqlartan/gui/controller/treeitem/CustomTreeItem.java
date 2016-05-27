package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import sqlartan.core.Database;
import sqlartan.gui.controller.SqlartanController;

public abstract class CustomTreeItem extends TreeItem {

	protected SqlartanController controller;
	protected Database database;
	private String name;

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
