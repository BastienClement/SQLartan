package sqlartan.view.treeitem;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.core.PersistentStructure;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.util.function.Consumer;

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
		MenuItem duplicate = new MenuItem("Duplicate");


		drop.setOnAction(openStructureDialog(this::dropDialog));
		rename.setOnAction(openStructureDialog(this::renameDialog));
		duplicate.setOnAction(openStructureDialog(this::duplicateDialog));

		return new ContextMenu(drop, rename, duplicate);
	}

	private EventHandler<ActionEvent> openStructureDialog(Consumer<PersistentStructure<?>> dialog) {
		return event -> SqlartanController.getDB().structure(name()).ifPresent(dialog);
	}

	private void duplicateDialog(PersistentStructure<?> structure) {
		Popup.input("Duplicate", "Name : ", structure.name()).ifPresent(name -> {
			if (name.length() > 0 && !structure.name().equals(name))
				controller.duplicateStructure(structure, name);
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
