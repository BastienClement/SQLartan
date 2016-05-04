package sqlartan.view.treeitem;

import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;

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

		drop.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(t -> {
			ButtonType yes = new ButtonType("YES");
			ButtonType no = new ButtonType("NO");
			Popup.warning("Drop table", "Are you sure to drop the table : " + t.name(), yes, no).ifPresent(type -> {
				if (type == yes) {
					controller.dropTable(t);
				}
			});
		}));

		rename.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(t -> {
			Popup.input("Rename", "Rename " + t.name() + " into : ", t.name()).ifPresent(name -> {
				if (name.length() > 0 && !t.name().equals(name))
					controller.renameTable(t, name);
			});
		}));

		copie.setOnAction(event -> SqlartanController.getDB().table(name()).ifPresent(t -> {
			Popup.input("Copy", "Name : ", t.name()).ifPresent(name -> {
				if (name.length() > 0 && !t.name().equals(name))
					controller.duplicateTable(t, name);
			});

		}));

		return new ContextMenu(drop, rename, copie);
	}

}
