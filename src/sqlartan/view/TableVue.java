package sqlartan.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Column;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Result;
import java.sql.SQLException;

/**
 * Created by julien on 29.04.16.
 */
public class TableVue {
	private ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();

	private TableView<ObservableList<String>> tableView = new TableView<>();

	private Database db = SqlartanController.getDB();

	private void dataView(Result res) {
		tableView.getColumns().clear();

		// Create columns
		int i = 0;
		for (Column c : res.columns()) {
			final int j = i++;
			TableColumn<ObservableList<String>, String> col = new TableColumn<>(c.name());
			col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
			tableView.getColumns().add(col);
		}

		// Add datas
		rows.clear();
		res.forEach(row -> rows.add(FXCollections.observableArrayList(
				res.columns().map(c -> row.getString()))
		));
		tableView.setItems(rows);

	}


	private void dataView(PersistentStructure<?> structure) throws SQLException {

		//sqlartan.getMainLayout().setCenter(tableView);
		dataView(structure.database().assemble("SELECT * FROM ", structure.fullName()).execute());

	}

	private void dataView(String str, Database db) throws SQLException {

		//sqlartan.getMainLayout().setCenter(tableView);
		dataView(db.execute(str));
	}


	TableView getTableView(PersistentStructure<?> structure) {
		try {
			dataView(structure);
		} catch (SQLException e) {
			showAlert(e);
		}

		return tableView;
	}

	/**
	 *
	 * @param str
	 * @return the table view
	 */
	TableView getTableView(String str) {
		try {
			dataView(str, db);
		} catch (SQLException e) {
			showAlert(e);
		}
		return tableView;
	}

	/**
	 * Show an alert
	 * @param e
	 */
	private void showAlert(SQLException e) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Bad Request");
		alert.setContentText(e.getMessage());

		alert.showAndWait();
	}
}

