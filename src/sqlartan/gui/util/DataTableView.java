package sqlartan.gui.util;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Column;
import sqlartan.core.Result;

/**
 * Created by julien on 29.04.16.
 */
public class DataTableView {


	/**
	 * Return a table gui for any result
	 *
	 * @param result the result
	 * @return the table gui
	 */
	public static TableView getTableView(Result result) {
		TableView<ObservableList<String>> tableView = new TableView<>();

		// Create columns
		int i = 0;
		for (Column c : result.columns()) {
			final int j = i++;
			TableColumn<ObservableList<String>, String> col = new TableColumn<>(c.name());
			col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
			tableView.getColumns().add(col);
		}

		// Add datas
		ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
		result.forEach(row -> rows.add(FXCollections.observableArrayList(
			result.columns().map(c -> row.getString()))
		));
		tableView.setItems(rows);

		return tableView;
	}
}

