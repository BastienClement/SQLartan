package sqlartan.gui.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Result;
import sqlartan.core.ResultColumn;

/**
 * Created by julien on 29.04.16.
 */
public class DataTableView {
	/**
	 * Return a table view for any result
	 *
	 * @param result the result
	 * @return the table gui
	 */
	public static TableView getTableView(Result result) {
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

