package sqlartan.gui.controller.treeitem;

import sqlartan.gui.controller.SqlartanController;

/**
 * Created by Adriano on 04.05.2016.
 */
public class ViewTreeItem extends StructureTreeItem {

	public ViewTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public Type type() {
		return Type.VIEW;
	}

	public String toString(){
		return "V – " + super.toString();
	}
}
