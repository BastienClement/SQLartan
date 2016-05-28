package sqlartan.gui.controller.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.Database;
import sqlartan.gui.controller.SqlartanController;
import sqlartan.gui.util.Popup;

public class DatabaseTreeItem extends CustomTreeItem {


	public DatabaseTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
		this.database = database;

	}


	/**
	 * {@inheritDoc}
	 * add a vacuum and a add table menu
	 */
	@Override
	public ContextMenu getMenu() {
		MenuItem vacuum = new MenuItem("Vacuum");
		MenuItem addTable = new MenuItem("Add table");
		MenuItem export = new MenuItem("Export");
		MenuItem importItem = new MenuItem("Import");

		vacuum.setOnAction(event -> controller.vacuum(database));
		addTable.setOnAction(event -> {
			Popup.input("Add table", "Name : ", "").ifPresent(name -> {
				if (name.length() > 0) {
					controller.addTable(database, name);
				}
			});
		});
		export.setOnAction(event -> controller.export(database));
		importItem.setOnAction(event -> controller.importDatabase(database));

		return new ContextMenu(addTable, export, importItem, vacuum);

	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
