package sqlartan.view;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.util.RuntimeSQLException;
import sqlartan.utils.Optionals;
import java.io.File;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private Sqlartan sqlartan;
	private Database db = null;
	private ObservableList<ObservableList<String>> rows = FXCollections.observableArrayList();
	@FXML
	private TreeView<String> treeView;
	@FXML
	private TableView tableView = new TableView();

	TreeItem<String> mainTreeItem;

	File file;

	/**
	 * Called by the mainApp to set the link to the mainApp
	 * @param sqlartan
	 */
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
		tableView.setEditable(true);
		tableView.setVisible(true);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			if (newValue != null) {
				Optionals.firstPresent(
						() -> db.table(newValue.getValue()),
						() -> db.view(newValue.getValue())
				).ifPresent(this::dataView);
			}

		});


		mainTreeItem = new TreeItem<>("Bases de données"); // Hidden
		mainTreeItem.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setRoot(mainTreeItem);
	}

	/**
	 * To call to refresh the view of the tree
	 * @throws SQLException
	 */
	void refreshView() throws SQLException
	{
		if (db != null){
			tree(db);
		}

	}

	/**
	 * Create the tree for a specific database
	 *
	 * @param database the database
	 * @throws SQLException
	 */
	void tree(Database database) throws SQLException {

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
	 * Close the entery application
	 */
	@FXML
	private void close()
	{
		Platform.exit();
	}


	/**
	 * Open a database
	 */
	@FXML
	private void openDB() throws SQLException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open SQLLite database");
		file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());

		while (true) {
			try {
				db = new Database(file.getPath());
				//db.execute("SELECT * FROM sqllite_master").toList();
				refreshView();
				break;
			} catch (SQLException e) {

				Alert alert = new Alert(Alert.AlertType.NONE);
				alert.setTitle("Problem while opening database");
				alert.setContentText("Error: " + e.getMessage());

				ButtonType buttonCanncel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
				ButtonType buttonRetry = new ButtonType("Retry");
				ButtonType buttonNewFile = new ButtonType("Choos new");

				alert.getButtonTypes().setAll(buttonNewFile, buttonRetry, buttonCanncel);


				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == buttonRetry) {
					continue;
				}
				else if (result.get() == buttonNewFile) {
					file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());
				}
				else{
					break;
				}

			}
		}
	}


	/**
	 * Close the current database
	 */
	@FXML
	private void closeDB() throws SQLException
	{
		tableView.getColumns().clear();
		mainTreeItem.getChildren().clear();
		db = null;
	}

	/**
	 * Truncate a table
	 *
	 * @param table
	 * @throws SQLException
	 */
	private void truncateTable(Table table) throws SQLException {
		table.truncate();
		refreshView();
	}

	/**
	 * Drop a table
	 *
	 * @param table
	 * @throws SQLException
	 */
	private void dropTable(Table table) throws SQLException {
		table.drop();
		refreshView();
	}

	/**
	 * Duplicate a table
	 *
	 * @param table
	 * @throws SQLException
	 */
	private void duplicateTable(Table table, String name) throws SQLException {
		table.duplicate(name);
		refreshView();
	}

	/**
	 * Rename a table
	 *
	 * @param table
	 * @param name
	 * @throws SQLException
	 */
	private void renameTable(Table table, String name) throws SQLException {
		table.rename(name);
		refreshView();
	}

	/**
	 * Vacuum a database
	 *
	 * @throws SQLException
	 */
	private void vacuumDatabase() throws SQLException {
		db.vacuum();
		refreshView();
	}

	/**
	 * Add a column to the specified table
	 *
	 * @param table
	 * @param name
	 * @param type
	 * @throws SQLException
	 */
	private void addColumn(Table table, String name, String type) throws SQLException {
		Affinity affinity = Affinity.forType(type);
		table.addColumn(name, affinity);
		refreshView();
	}

	/**
	 * Drop the specified column from the table
	 *
	 * @param table
	 * @param name
	 * @throws SQLException
	 */
	private void dropColumn(Table table, String name) throws SQLException {
		if(table.column(name).isPresent()){
			table.column(name).get().drop();
		}
		refreshView();
	}

	/**
	 * Rename the specified column from the table
	 *
	 * @param table
	 * @param name
	 * @param newName
	 * @throws SQLException
	 */
	private void renameColumn(Table table, String name, String newName) throws SQLException {
		if(table.column(name).isPresent()){
			table.column(name).get().rename(newName);
		}
		refreshView();
	}

	/**
	 * Attach a database to the main database
	 *
	 * @param fileName
	 * @throws SQLException
	 */
	private void attachDatabase(String fileName, String databaseName) throws SQLException {
		db.attach(fileName, databaseName);
		refreshView();
	}

	/**
	 * Detach a database from the main database
	 *
	 * @param database
	 * @throws SQLException
	 */
	private void detachDatabase(Database database) throws SQLException {
		db.detach(database.name());
		refreshView();
	}
}
