package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

public abstract class CustomTreeItem extends TreeItem {

	private String name;
	private Type type;

	public ContextMenu itemContextMenu;

	public CustomTreeItem(String name, Type type) {
		this.name = name;
		this.type = type;

	}

	public abstract ContextMenu getMenu();

	/**
	 * @return the type of the treeItem
	 */
	public Type type() {
		return type;
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
