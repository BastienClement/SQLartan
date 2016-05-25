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

		ChoiceBox cb = new ChoiceBox<>(FXCollections.observableArrayList("TEXT", "INTEGER", "NULL", "REAL", "BLOB"));
		cb.getSelectionModel().selectFirst();
		
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

		dialog.showAndWait().ifPresent(values -> controller.addColumn(table, values.getKey(), values.getValue()));
	}

	@Override
	public Type type() {
		return Type.TABLE;
	}
}
