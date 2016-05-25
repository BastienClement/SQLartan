package sqlartan.view.treeitem;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import sqlartan.core.Table;
import sqlartan.view.SqlartanController;
import java.util.ArrayList;
import java.util.function.Consumer;

public class TableTreeItem extends StructureTreeItem {

	public TableTreeItem(String name, SqlartanController controller) {
		super(name, controller);
	}

	@Override
	public ContextMenu getMenu() {
		MenuItem truncate = new MenuItem("Truncate");
		MenuItem addColumn = new MenuItem("Add column");

		truncate.setOnAction(openStructureDialog(this::truncateDialog));
		addColumn.setOnAction(openStructureDialog(this::addColumnDialog));

		ContextMenu res = super.getMenu();
		res.getItems().add(truncate);
		res.getItems().add(addColumn);
		return res;
	}

	private EventHandler<ActionEvent> openStructureDialog(Consumer<Table> dialog) {
		return event -> SqlartanController.getDB().table(name()).ifPresent(dialog);
	}

	private void truncateDialog(Table table) {
		controller.truncateTable(table);
	}

	private void addColumnDialog(Table table) {
		class AddColumnResult {
			private String name, type;
			private boolean unique, primary, nullable;

			public AddColumnResult(String name, String type, boolean unique, boolean primary, boolean nullable) {
				this.name = name;
				this.type = type;
				//this.check = check == "" ? null : check;
				this.unique = unique;
				this.primary = primary;
				this.nullable = nullable;
			}
		}

		// Create the custom dialog.
		Dialog<AddColumnResult> dialog = new Dialog<>();
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

		TextField name = new TextField();

		ChoiceBox type = new ChoiceBox<>(FXCollections.observableArrayList("TEXT", "INTEGER", "NULL", "REAL", "BLOB"));
		type.getSelectionModel().selectFirst();

		CheckBox unique = new CheckBox();
		CheckBox primary = new CheckBox();
		CheckBox nullable = new CheckBox();

		//TextField check = new TextField();
		
		grid.add(new Label("Name : "), 0, 0);
		grid.add(name, 1, 0);
		grid.add(new Label("Type : "), 0, 1);
		grid.add(type, 1, 1);
		grid.add(new Label("Unique : "), 0, 2);
		grid.add(unique, 1, 2);
		grid.add(new Label("Primary : "), 0, 3);
		grid.add(primary, 1, 3);
		grid.add(new Label("Nullable : "), 0, 4);
		grid.add(nullable, 1, 4);
		//grid.add(new Label("Check : "), 0, 5);
		//grid.add(check, 1, 5);

		dialog.getDialogPane().setContent(grid);



		// Convert the result to a username-password-pair when the button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				return new AddColumnResult(name.getText(), type.getValue().toString(), unique.isSelected(), primary.isSelected(), nullable.isSelected());
			}
			return null;
		});
		dialog.showAndWait().ifPresent(addColumnResult -> {
			controller.addColumn(table, addColumnResult.name, addColumnResult.type, addColumnResult.unique, addColumnResult.primary, addColumnResult.nullable);
		});
	}

	@Override
	public Type type() {
		return Type.TABLE;
	}
}
