package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;

public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");
		MenuItem addColumn = new MenuItem("Add column");

		truncate.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(table -> {
			controller.truncateTable(table);
		}));
		addColumn.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(table -> {
			// TODO
			Popup.input("Add column", "Name : ", "").ifPresent(name -> {
				if (name.length() > 0){

				}
			});
			//controller.addColumn();
		}));

		ContextMenu res = super.getMenu();
		res.getItems().add(truncate);
		res.getItems().add(addColumn);
		return res;
	}
	@Override
	public Type type() {
		return Type.TABLE;
	}
}
