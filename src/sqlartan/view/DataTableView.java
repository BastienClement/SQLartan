package sqlartan.view;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import sqlartan.core.*;
import sqlartan.view.util.Popup;
import java.sql.SQLException;

/**
 * Created by julien on 29.04.16.
 */
public class DataTableView {

	/**
	 * Return a table view for any result
	 *
	 * @param result the result
	 * @return the table view
	 */
	public TableView getTableView(Result result) {
		TableView<ObservableList<EditBidon>> tableView = new TableView<>();

		// Create columns
		int i = 0;
		for (ResultColumn c : result.columns()) {
			final int j = i++;
			TableColumn<ObservableList<EditBidon>, EditBidon> col = new TableColumn<>(c.name());
			col.setCellValueFactory(tc -> new SimpleObjectProperty<>(tc.getValue().get(j)));
			col.setCellFactory(tc -> new EditCell());
			tableView.getColumns().add(col);
		}

		// Add datas
		ObservableList<ObservableList<EditBidon>> rows = FXCollections.observableArrayList();
		result.forEach(row -> rows.add(FXCollections.observableArrayList(
			result.columns().map(c -> new EditBidon(row, c, row.getString())))
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

