package sqlartan.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private static Database db = null;
	TreeItem<DbTreeItem> mainTreeItem;
	File file;
	private Sqlartan sqlartan;
	@FXML
	private TreeView<DbTreeItem> treeView;
	@FXML
	private BorderPane borderPane;
	@FXML
	private StackPane stackPane;
	static public Database getDB() {
		return db;
	}
	@FXML
	private void initialize() throws SQLException {

		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			stackPane.getChildren().clear();
			if (newValue != null) {
				TabPane tabPane = null;
				switch (newValue.getValue().type()) {
					case DATABASE: {
						try {
							tabPane = new FXMLLoader(Sqlartan.class.getResource("view/DatabaseTabs.fxml")).load();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					break;
					case TABLE:
					case VIEW: {
						// Onglets
						FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/TableTabs.fxml"));

						try {
							tabPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						DbTreeItem treeItem = newValue.getValue();
						Optional<? extends PersistentStructure<?>> structure = Optional.empty();
						switch (treeItem.type()) {
							case TABLE:
								structure = db.table(treeItem.name());
								break;
							case VIEW:
								structure = db.view(treeItem.name());
								break;
						}

						TableTabsController tabsController = loader.getController();
						structure.ifPresent(tabsController::setStructure);

					}
					break;
				}

				stackPane.getChildren().add(tabPane);
				tabPane.prefHeightProperty().bind(stackPane.heightProperty());
				tabPane.prefWidthProperty().bind(stackPane.widthProperty());
			}
		});

		mainTreeItem = new TreeItem<>(); // Hidden
		mainTreeItem.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setRoot(mainTreeItem);
	}
	/**
	 * Called by the mainApp to set the link to the mainApp
	 *
	 * @param sqlartan
	 */
	public void setApp(Sqlartan sqlartan) {
		this.sqlartan = sqlartan;
	}
	/**
	 * To call to refresh the view of the tree
	 *
	 * @throws SQLException
	 */
	void refreshView() throws SQLException {
		if (db != null) {
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
		TreeItem<DbTreeItem> trees = new TreeItem<>(new DbTreeItem(database.name(), Type.DATABASE));

		trees.getChildren().addAll(database.tables()
		                                   .map(Table::name) // .map(table -> table.name())
		                                   .map(name -> new DbTreeItem(name, Type.TABLE)) // flux de dbtreeitme
		                                   .map(TreeItem::new)
		                                   .toList());


		trees.getChildren().addAll(database.views()
		                                   .map(View::name)
		                                   .map(name -> new DbTreeItem(name, Type.VIEW))
		                                   .map(TreeItem::new)
		                                   .toList());

		mainTreeItem.getChildren().add(trees);

		// Attached database
		database.attached().values().forEach(adb -> {
			TreeItem<DbTreeItem> tItems = new TreeItem<>(new DbTreeItem(adb.name(), Type.DATABASE));
			tItems.getChildren().addAll(adb.tables()
			                               .map(Table::name)
			                               .map(name -> new DbTreeItem(name, Type.TABLE))
			                               .map(TreeItem::new)
			                               .toList());

			tItems.getChildren().addAll(adb.views()
			                               .map(View::name)
			                               .map(name -> new DbTreeItem(name, Type.VIEW))
			                               .map(TreeItem::new)
			                               .toList());

			mainTreeItem.getChildren().add(tItems);
		});

	}
	/**
	 * Open a database
	 */
	@FXML
	private void openDB() {
		// Create the file popup
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open SQLite database");
		file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());

		while (true) {
			try {
				if (file == null)
					break;
				db = Database.open(file.getPath());
				refreshView();
				break;
			} catch (SQLException e) {

				Alert alert = new Alert(Alert.AlertType.NONE);
				alert.setTitle("Problem while opening database");
				alert.setContentText("Error: " + e.getMessage());

				ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
				ButtonType buttonRetry = new ButtonType("Retry");
				ButtonType buttonNewFile = new ButtonType("Choos new");

				alert.getButtonTypes().setAll(buttonNewFile, buttonRetry, buttonCancel);

				Optional<ButtonType> result = alert.showAndWait();

				if (result.isPresent()) {
					if (result.get() == buttonNewFile) {
						file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());
					} else if (result.get() == buttonCancel) {
						break;
					}
				}
			}
		}
	}
	/**
	 * Close the current database
	 */
	@FXML
	private void closeDB() throws SQLException {
		mainTreeItem.getChildren().clear();
		stackPane.getChildren().clear();
		db.close();
	}
	/**
	 * Close the entery application
	 */
	@FXML
	private void close() {
		Platform.exit();
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
		if (table.column(name).isPresent()) {
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
		if (table.column(name).isPresent()) {
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
