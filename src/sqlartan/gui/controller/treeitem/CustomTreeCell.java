package sqlartan.gui.controller.treeitem;

import javafx.scene.control.TreeCell;

/**
 * The custom tree cell, add the right click contextual menu on a TreeCell
 */
public class CustomTreeCell extends TreeCell<CustomTreeItem> {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateItem(CustomTreeItem item, boolean empty) {
		super.updateItem(item, empty);

		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			setText(getItem() == null ? "" : getItem().toString());
			setGraphic(getTreeItem().getGraphic());
			setContextMenu((getTreeItem().getValue()).getMenu());
		}
	}
}