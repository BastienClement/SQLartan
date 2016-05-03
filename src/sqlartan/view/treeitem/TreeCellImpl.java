package sqlartan.view.treeitem;

import javafx.scene.control.TreeCell;

public class TreeCellImpl extends TreeCell<CustomTreeItem>
{
	@Override
	public void updateItem(CustomTreeItem item, boolean empty)
	{
		super.updateItem(item, empty);

		if (empty)
		{
			setText(null);
			setGraphic(null);
		} else
		{
			setText(getItem() == null ? "" : getItem().toString());
			setGraphic(getTreeItem().getGraphic());
			setContextMenu((getTreeItem().getValue()).getMenu());
		}
	}
}