package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;
import sqlartan.view.SqlartanController;

public abstract class CustomTreeItem extends TreeItem {

	private String name;
	protected SqlartanController controller;

	public CustomTreeItem(String name, SqlartanController controller) {
		this.name = name;
		this.controller = controller;

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
