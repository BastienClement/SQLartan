package sqlartan.view.treeitem;

import javafx.scene.control.*;
import sqlartan.view.SqlartanController;
import java.sql.SQLException;

public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");

		truncate.setOnAction(event -> {
			try {
				controller.truncateTable(SqlartanController.getDB().table(name()).get());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		ContextMenu res = super.getMenu();
		res.getItems().add(truncate);
		return res;
	}
	@Override
	public Type type() {
		return Type.TABLE;
	}
}
