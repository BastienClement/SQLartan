package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;

public class DatabaseTreeItem extends CustomTreeItem {

	public DatabaseTreeItem(String name, SqlartanController controller) {
		super(name, controller);

	}

	@Override
	public ContextMenu getMenu() {
		MenuItem vacuum = new MenuItem("Vacuum");
		vacuum.setOnAction(event -> {
			SqlartanController.getDB().vacuum();
			Popup.information("Vacuum", "The database " + SqlartanController.getDB().name() + " get vacuumed");
		});

		return new ContextMenu(vacuum);

	}
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
