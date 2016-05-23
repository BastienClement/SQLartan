package sqlartan.view.treeitem;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import sqlartan.view.SqlartanController;
import sqlartan.view.util.Popup;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
			// Create the custom dialog.
			Dialog<Pair<String, String>> dialog = new Dialog<>();
			dialog.setTitle("Add column");
			dialog.setHeaderText(null);

			// Set the button types.
			ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
			dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

			// Create the two labels and fields
			GridPane grid = new GridPane();
			grid.setHgap(10);
			grid.setVgap(10);
			grid.setPadding(new Insets(20, 150, 10, 10));

			TextField nameField = new TextField();

			ChoiceBox cb = new ChoiceBox(FXCollections.observableArrayList(
				"TEXT", "INTEGER", "NULL", "REAL", "BLOB")
			);

			grid.add(new Label("Name : "), 0, 0);
			grid.add(nameField, 1, 0);
			grid.add(new Label("Type : "), 0, 1);
			grid.add(cb, 1, 1);

			dialog.getDialogPane().setContent(grid);

			// Convert the result to a username-password-pair when the login button is clicked.
			dialog.setResultConverter(dialogButton -> {
				if (dialogButton == okButtonType) {
					return new Pair<>(nameField.getText(), cb.getValue().toString());
				}
				return null;
			});

			Optional<Pair<String, String>> result = dialog.showAndWait();

			result.ifPresent(values -> {
				controller.addColumn(table, values.getKey(), values.getValue());
				System.out.println(table.name() + " " + values.getKey() + " " + values.getValue());
			});
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
