package sqlartan.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Column;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Result;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

/**
 * Created by julien on 29.04.16.
 */
public class DataTableView {

	private Database db = SqlartanController.getDB();

	/**
	 * Return a table view for any result
	 *
	 * @param result the result
	 * @return the table view
	 */
	public TableView getTableView(Result result) {
		TableView<ObservableList<String>> tableView = new TableView<>();

		// Create columns
		int i = 0;
		for (Column c : result.columns()) {
			final int j = i++;
			TableColumn<ObservableList<String>, String> col = new TableColumn<>(c.name());
			col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
			col.setCellFactory(param -> new EditCell());
			col.setOnEditCommit(event ->
				event.getTableView().getItems().get(event.getTablePosition().getRow()).set(j, event.getNewValue())
			);
			tableView.getColumns().add(col);
		}

		// Add datas
		ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
		result.forEach(row -> rows.add(FXCollections.observableArrayList(
			result.columns().map(c -> row.getString()))
		));
		tableView.setEditable(true);
		tableView.setItems(rows);

		return tableView;
	}

	/**
	 * Return a table view for any type of structure
	 *
	 * @param structure the structure
	 * @return the table view
	 */
	public TableView getTableView(PersistentStructure<?> structure) {
		try {
			return getTableView(structure.database().assemble("SELECT * FROM ", structure.fullName()).execute());
		} catch (SQLException e) {
			Popup.error("Bad Request", e.getMessage());
			return new TableView();
		}

	}
}

