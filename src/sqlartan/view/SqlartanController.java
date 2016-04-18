package sqlartan.view;

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
import java.sql.SQLException;
import java.util.Observable;

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
	@FXML

	private void initialize() throws SQLException {
		Database db = new Database("testdb.db");
		tree(db);
		tableView.setEditable(true);
		tableView.setVisible(true);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			db.table(newValue.getValue()).ifPresent(this::structure);
			db.table(newValue.getValue()).ifPresent(this::dataView);
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

		/* TODO remove when view is coded
		trees.getChildren().addAll(database.views()
		                                   .map(View::name)
		                                   .map(TreeItem::new)
		                                   .toList());
		 */

		mainTreeItem.getChildren().add(trees);

		// Attached database
		for (AttachedDatabase adb : database.attached().values()) {
			TreeItem<String> tItems = new TreeItem<>(adb.name());
			tItems.getChildren().addAll(adb.tables()
			                               .map(Table::name)
			                               .map(TreeItem::new)
			                               .toList());

			/* TODO remove when view is coded
			for (View view : database.views())
				tItems.getChildren().add(new TreeItem<>(view.name()));
			*/
			mainTreeItem.getChildren().add(tItems);
		}
	}

	void structure(Table t) {
		/*
		tableView.getColumns().clear();
		tableView.getColumns().addAll(t.columns()
		                               .map(Column::name)
		                               .map(TableColumn::new)
		                               .toList());
		                               */
	}


	void dataView(Table table) {
		Database db = table.database();
		String query = db.format("SELECT * FROM ", table.name());
		Result res;
		try {
			res = db.execute(query);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}

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
		for (Row resRow : res) {
			ObservableList<String> row = FXCollections.observableArrayList();
			for (int k = 1; k <= res.columns().count(); k++) {
				row.add(resRow.getString());
			}
			System.out.println("Row [1] added " + row);
			rows.add(row);
		}


	}


}
