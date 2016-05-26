package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.gui.controller.SqlartanController;
import sqlartan.gui.util.Popup;
import sqlartan.core.Database;

public class DatabaseTreeItem extends CustomTreeItem {


	public DatabaseTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
		this.database = database;

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public ContextMenu getMenu() {
		MenuItem vacuum = new MenuItem("Vacuum");
		MenuItem addTable = new MenuItem("Add table");

		vacuum.setOnAction(event -> controller.vacuum(database));
		addTable.setOnAction(event -> {
			Popup.input("Add table", "Name : ", "").ifPresent(name -> {
				if (name.length() > 0) {
					controller.addTable(database, name);
				}
			});
			//controller.addColumn();
		});

		return new ContextMenu(vacuum, addTable);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
