package sqlartan.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import sqlartan.core.Column;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Result;
import sqlartan.core.Table;
import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;

/**
 * Created by julien on 29.04.16.
 */
public class TableVue
{
	private ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();

	private TableView<ObservableList<String>> tableView = new TableView<>();


	private void dataView(Result res, TableView<ObservableList<String>> tv) {
		tv.getColumns().clear();

		//Creations des colones
		int i = 0;
		for (Column c : res.columns()) {
			final int j = i;
			TableColumn<ObservableList<String>, String> col = new TableColumn<>(c.name());
			col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(j)));
			tv.getColumns().add(col);
			//System.out.println("Column [" + i++ + "] " + c.name());
		}

		//Ajout des donnees
		rows.clear();
		res.forEach(row -> rows.add(FXCollections.observableArrayList(
				res.columns().map(c -> row.getString()))
		));
		tv.setItems(rows);

	}


	void dataView(PersistentStructure<?> structure) {
		try {
			//sqlartan.getMainLayout().setCenter(tableView);
			dataView(structure.database().assemble("SELECT * FROM ", structure.name()).execute(), tableView);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}


	TableView getTableView(PersistentStructure<?> structure)
	{
		dataView(structure);
		return tableView;
	}
}
