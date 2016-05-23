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

		drop.setOnAction(event -> SqlartanController.getDB().structure(name()).ifPresent(structure -> {
			ButtonType yes = new ButtonType("YES");
			ButtonType no = new ButtonType("NO");
			String structTypeName = this.type().toString().toLowerCase();
			Popup.warning("Drop " + structTypeName, "Are you sure to drop the " + structTypeName + " : " + structure.name(), yes, no)
			     .filter(b -> b == yes)
			     .ifPresent(b -> controller.dropStructure(structure));
		}));

		rename.setOnAction(event -> SqlartanController.getDB().structure(name()).ifPresent(structure -> {
			Popup.input("Rename", "Rename " + structure.name() + " into : ", structure.name()).ifPresent(name -> {
				if (name.length() > 0 && !structure.name().equals(name))
					controller.renameTable(structure, name);
			});
		}));

		copie.setOnAction(event -> SqlartanController.getDB().structure(name()).ifPresent(structure -> {
			Popup.input("Copy", "Name : ", structure.name()).ifPresent(name -> {
				if (name.length() > 0 && !structure.name().equals(name))
					controller.duplicateTable(structure, name);
			});

		}));

		return new ContextMenu(drop, rename, copie);
	}

}
