package sqlartan.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sqlartan.Sqlartan;
import sqlartan.core.AttachedDatabase;
import sqlartan.core.Column;
import sqlartan.core.Database;
import sqlartan.core.Table;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private Sqlartan sqlartan;
	private ObservableList<testClass> rows = FXCollections.observableArrayList();
	@FXML
	private TreeView<String> treeView;
	@FXML
	private TableView table = new TableView();
	public void setApp(Sqlartan sqlartan) {
		this.sqlartan = sqlartan;
	}
	@FXML
	private void initialize() throws SQLException {
		Database db = new Database("testdb.db");
		tree(db);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			Optional<Table> oTable = db.table(newValue.getValue());
			if (oTable.isPresent()) {
				structure(db.table(newValue.getValue()).get());
			}
		});
		// Table t = db.table("table1").get();
		//structure(t);

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
		for (Table table : database.tables())
			trees.getChildren().add(new TreeItem<>(table.name()));
		/* TODO remove when view is coded
		for (View view : database.views())
			trees.getChildren().add(new TreeItem<>(view.name()));
		*/
		mainTreeItem.getChildren().add(trees);

		// Attached database
		for (AttachedDatabase adb : database.attached().values()) {
			TreeItem<String> tItems = new TreeItem<>(adb.name());
			for (Table table : adb.tables())
				tItems.getChildren().add(new TreeItem<>(table.name()));
			/* TODO remove when view is coded
			for (View view : database.views())
				tItems.getChildren().add(new TreeItem<>(view.name()));
			*/
			mainTreeItem.getChildren().add(tItems);
		}
	}

	void structure(Table t) {
		table.getColumns().clear();
		table.setEditable(true);

		for (Column c : t.columns()) {
			TableColumn tc = new TableColumn(c.name());
			table.getColumns().add(tc);
		}
		table.setVisible(true);

	}

	/*
	void test2() {
		table.setItems(rows);
		testClass tmp = rows.get(0);
		rows.add

		for (int i = 0; i < 10; ++i) {
			TableColumn<testClass, String> coll = new TableColumn<>();
			table.getColumns().add(coll);
			coll.setCellValueFactory(cellDate -> cellDate.getValue().tab.get(i));
		}
	}
	*/


}
