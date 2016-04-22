package sqlartan.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.util.RuntimeSQLException;
import sqlartan.utils.Optionals;
import java.sql.SQLException;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private Sqlartan sqlartan;
	private ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
	@FXML
	private TreeView<String> treeView;
	@FXML
	private TableView tableView = new TableView();
	public void setApp(Sqlartan sqlartan) {
		this.sqlartan = sqlartan;
	}

	void dataView(PersistentStructure<?> structure) {
		try {
			dataView(structure.database().assemble("SELECT * FROM ", structure.name()).execute());
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	@FXML
	private void initialize() throws SQLException {
		Database db = new Database("testdb.db");
		tree(db);
		tableView.setEditable(true);
		tableView.setVisible(true);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Optionals.firstPresent(
					() -> db.table(newValue.getValue()),
					() -> db.view(newValue.getValue())
			).ifPresent(this::dataView);

		});
	}

	/**
	 * Create the tree for a specific database
	 *
	 * @param database the database
	 * @throws SQLException
	 */
	void tree(Database database) throws SQLException {
		TreeItem<String> mainTreeItem = new TreeItem<>("Bases de données"); // Hidden
		mainTreeItem.setExpanded(true);

		treeView.setShowRoot(false);
		treeView.setRoot(mainTreeItem);

		// Main
		TreeItem<String> trees = new TreeItem<>(database.name());

		trees.getChildren().addAll(database.tables()
		                                   .map(Table::name)
		                                   .map(TreeItem::new)
		                                   .toList());


		trees.getChildren().addAll(database.views()
		                                   .map(View::name)
		                                   .map(TreeItem::new)
		                                   .toList());

		mainTreeItem.getChildren().add(trees);

		// Attached database
		for (AttachedDatabase adb : database.attached().values()) {
			TreeItem<String> tItems = new TreeItem<>(adb.name());
			tItems.getChildren().addAll(adb.tables()
			                               .map(Table::name)
			                               .map(TreeItem::new)
			                               .toList());

			tItems.getChildren().addAll(adb.views()
			                               .map(View::name)
			                               .map(TreeItem::new)
			                               .toList());

			mainTreeItem.getChildren().add(tItems);
		}
	}

	void dataView(Result res) {
		tableView.getColumns().clear();
		/**********************************
		 * TABLE COLUMN ADDED DYNAMICALLY *
		 **********************************/
		int i = 0;
		for (Column c : res.columns()) {
			final int j = i;
			TableColumn col = new TableColumn(c.name());
			col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList, String>, ObservableValue<String>>() {
				public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
					return new SimpleStringProperty(param.getValue().get(j).toString());
				}
			});
			tableView.getColumns().addAll(col);
			System.out.println("Column [" + i++ + "] " + c.name());
		}

		/********************************
		 * Data added to ObservableList *
		 ********************************/
		rows.clear();
		res.forEach(row -> rows.add(FXCollections.observableArrayList(
				res.columns().map(c -> row.getString()))
		));
		tableView.setItems(rows);

	}

	/**
	 * The methode called by the close button
	 */
	@FXML
	private void close()
	{
		Platform.exit();
	}
}
