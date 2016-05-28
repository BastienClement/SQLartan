package sqlartan.gui.controller.treeitem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import sqlartan.gui.controller.SqlartanController;
import sqlartan.gui.util.Popup;
import java.util.function.Consumer;

/**
 * Created by Adriano on 04.05.2016.
 */
public abstract class StructureTreeItem extends CustomTreeItem {
	public StructureTreeItem(String name, SqlartanController controller, Database database) {
		super(name, controller, database);
	}

	@Override
	public ContextMenu getMenu() {

		MenuItem drop = new MenuItem("Drop");
		MenuItem rename = new MenuItem("Rename");
		MenuItem duplicate = new MenuItem("Duplicate");


		drop.setOnAction(openStructureDialog(this::dropDialog));
		rename.setOnAction(openStructureDialog(this::renameDialog));
		duplicate.setOnAction(openStructureDialog(this::duplicateDialog));

		return new ContextMenu(drop, rename, duplicate);
	}

	private EventHandler<ActionEvent> openStructureDialog(Consumer<PersistentStructure<?>> dialog) {
		return event -> database.structure(name()).ifPresent(dialog);
	}

	private void duplicateDialog(PersistentStructure<?> structure) {
		Popup.input("Duplicate", "Name : ", structure.name()).ifPresent(name -> {
			if (name.length() > 0 && !structure.name().equals(name))
				structure.duplicate(name);
			controller.refreshView();
		});
	}

	private void renameDialog(PersistentStructure<?> structure) {
		Popup.input("Rename", "Rename " + structure.name() + " into : ", structure.name()).ifPresent(name -> {
			if (name.length() > 0 && !structure.name().equals(name)) {
				controller.renameStructure(structure, name);
			} else {
				Popup.error("Rename error", "The name is already used or don't have enough chars");
			}
		});
	}

	private void dropDialog(PersistentStructure<?> structure) {
		ButtonType yes = new ButtonType("YES");
		ButtonType no = new ButtonType("NO");
		String structTypeName = this.type().toString().toLowerCase();
		Popup.warning("Drop " + structTypeName, "Are you sure to drop the " + structTypeName + " : " + structure.name(), yes, no)
		     .filter(b -> b == yes)
		     .ifPresent(b -> controller.dropStructure(structure));
	}
}
