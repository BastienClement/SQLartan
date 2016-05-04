package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;

public class DbTreeItem extends CustomTreeItem {

	public DbTreeItem(String name, SqlartanController controller) {
		super(name, controller);

	}

	@Override
	public ContextMenu getMenu() {
		MenuItem tabMenu1 = new MenuItem("Vacuum");
		tabMenu1.setOnAction(event -> System.out.println("Coucou"));
		return new ContextMenu(tabMenu1);

	}
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
