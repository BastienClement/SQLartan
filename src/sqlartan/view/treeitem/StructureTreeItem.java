package sqlartan.view.treeitem;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.Table;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

/**
 * Created by Adriano on 04.05.2016.
 */
public abstract class StructureTreeItem extends CustomTreeItem {
	public StructureTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {

		MenuItem drop = new MenuItem("Drop");
		MenuItem rename = new MenuItem("Rename");
		MenuItem copie = new MenuItem("Copy");

		drop.setOnAction(event -> {
			try {
				Table t = SqlartanController.getDB().table(name()).get();

				ButtonType yes = new ButtonType("YES");
				ButtonType no = new ButtonType("NO");
				if(Popup.warning("Drop table", "Are you sure to drop the table : " + t.name(), yes, no) == yes)
					controller.dropTable(t);

			}
			catch (SQLException e) {
				Popup.error("Drop error", e.getMessage());
			}
		});
		rename.setOnAction(event -> {
			// TODO menu with the new name
		});
		copie.setOnAction(event -> {
			Table t = SqlartanController.getDB().table(name()).get();
			try {
				controller.duplicateTable(t, t.name() + "_Copie");
			} catch (SQLException e) {
				Popup.error("Copie error", e.getMessage());
			}
		});

		return new ContextMenu(drop, rename, copie);
	}

}
