package sqlartan.view.treeitem;

import javafx.scene.control.TreeCell;
import sqlartan.view.SqlartanController;

public class TreeCellImpl extends TreeCell<CustomTreeItem>
{

	protected SqlartanController sqlartanController;

	public TreeCellImpl(SqlartanController sqlartanController){
		this.sqlartanController = sqlartanController;
	}
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