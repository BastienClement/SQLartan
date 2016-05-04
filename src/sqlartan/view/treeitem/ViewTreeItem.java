package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import sqlartan.view.SqlartanController;

/**
 * Created by Adriano on 04.05.2016.
 */
public class ViewTreeItem extends CustomTreeItem {

	public ViewTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		return new ContextMenu();
	}
	@Override
	public Type type() {
		return Type.VIEW;
	}
}
