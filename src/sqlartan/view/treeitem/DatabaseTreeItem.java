package sqlartan.view.treeitem;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import sqlartan.view.SqlartanController;
import java.sql.SQLException;

public class DatabaseTreeItem extends CustomTreeItem {

	public DatabaseTreeItem(String name, SqlartanController controller) {
		super(name, controller);

	}

	@Override
	public ContextMenu getMenu() {
		MenuItem vacuum = new MenuItem("Vacuum");
		vacuum.setOnAction(event -> {
			try {
				controller.vacuumDatabase();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});

		return new ContextMenu(vacuum);

	}
	@Override
	public Type type() {
		return Type.DATABASE;
	}
}
