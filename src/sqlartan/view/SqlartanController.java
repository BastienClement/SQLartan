package sqlartan.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import sqlartan.Sqlartan;
import sqlartan.core.*;
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
		table.setEditable(true);
		table.setVisible(true);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			db.table(newValue.getValue()).ifPresent(this::structure);
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
		table.getColumns().clear();
		table.getColumns().addAll(t.columns()
		                           .map(Column::name)
		                           .map(TableColumn::new)
		                           .toList());
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
