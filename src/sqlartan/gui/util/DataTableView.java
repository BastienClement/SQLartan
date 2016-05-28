package sqlartan.gui.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Result;
import sqlartan.core.ResultColumn;

/**
 * Represent the data in a TableView
 */
public class DataTableView {
	/**
	 * Return a table view for any result.
	 * The cells of the table can be edit by clicking on them. A text field will be created if the cell is editable
	 *
	 * @param result the result
	 * @return the table view
	 */
	public static TableView<ObservableList<EditModel>> getTableView(Result result) {
		TableView<ObservableList<EditModel>> tableView = new TableView<>();

		// Create columns
		int i = 0;
		for (ResultColumn c : result.columns()) {
			final int j = i++;
			TableColumn<ObservableList<EditModel>, EditModel> col = new TableColumn<>(c.name());
			col.setCellValueFactory(tc -> new SimpleObjectProperty<>(tc.getValue().get(j)));
			col.setCellFactory(tc -> new EditCell());
			tableView.getColumns().add(col);
		}

		// Add datas
		ObservableList<ObservableList<EditModel>> rows = FXCollections.observableArrayList();
		result.forEach(row -> rows.add(FXCollections.observableArrayList(
			result.columns().map(c -> new EditModel(row, c, row.getString())))
		));
		tableView.setEditable(true);
		tableView.setItems(rows);

		return tableView;
	}
}

