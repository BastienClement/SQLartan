package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

public class TableTreeItem extends CustomTreeItem {


	public TableTreeItem(String name, Type type) {
		super(name, type);
	}

	@Override
	public ContextMenu getMenu() {

		MenuItem tabMenu1 = new MenuItem("Table");
		tabMenu1.setOnAction(event -> System.out.println("Menu Item Clicked!"));
		return new ContextMenu(tabMenu1);
	}
}
