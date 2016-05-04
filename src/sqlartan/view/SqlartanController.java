package sqlartan.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.view.attached.AttachedChooserController;
import sqlartan.view.tabs.DatabaseTabsController;
import sqlartan.view.tabs.TableTabsController;
import sqlartan.view.treeitem.*;
import sqlartan.view.util.Popup;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Created by guillaume on 04.04.16.
 */
public class SqlartanController {

	private static Database db = null;

	TreeItem<CustomTreeItem> mainTreeItem;

	private Sqlartan sqlartan;
	@FXML
	private TreeView<CustomTreeItem> treeView;
	@FXML
	private BorderPane borderPane;
	@FXML
	private StackPane stackPane;
	@FXML
	private Menu detatchMenu;
	@FXML
	private Menu databaseMenu;

	private List<String> atachedDBs = new LinkedList<>();


	/***********
	 * METHODES*
	 ***********/


	static public Database getDB() {
		return db;
	}

	/**
	 * First methode call when loaded
	 */
	@FXML
	private void initialize() throws SQLException {

		treeView.setCellFactory(param -> new TreeCellImpl(this));

		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			stackPane.getChildren().clear();
			if (newValue != null) {
				TabPane tabPane = null;
				switch (newValue.getValue().type()) {
					case DATABASE: {
						FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/DatabaseTabs.fxml"));
						try {
							tabPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						DatabaseTabsController tabsController = loader.getController();
						tabsController.setDatabase(db);
					}

					break;
					case TABLE:
					case VIEW: {
						// Onglets
						FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/TableTabs.fxml"));

						try {
							tabPane = loader.load();
						} catch (IOException e) {
							e.printStackTrace();
						}

						CustomTreeItem treeItem = newValue.getValue();
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

		mainTreeItem = new TreeItem<>(); // Hidden fake root
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
			mainTreeItem.getChildren().clear();
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
		TreeItem<CustomTreeItem> trees = new TreeItem<>(new DbTreeItem(database.name(), this));

		trees.getChildren().addAll(database.tables()
		                                   .map(Table::name) // .map(table -> table.name())
		                                   .map(name -> (CustomTreeItem) new TableTreeItem(name, this)) // flux de dbtreeitme
		                                   .map(TreeItem::new)
		                                   .toList());


		trees.getChildren().addAll(database.views()
		                                   .map(View::name)
		                                   .map(name -> (CustomTreeItem) new ViewTreeItem(name, this))
		                                   .map(TreeItem::new)
		                                   .toList());

		mainTreeItem.getChildren().add(trees);

		// Attached database
		database.attached().values().forEach(adb -> {
			TreeItem<CustomTreeItem> tItems = new TreeItem<>(new DbTreeItem(adb.name(), this));
			tItems.getChildren().addAll(adb.tables()
			                               .map(Table::name)
			                               .map(name -> (CustomTreeItem) new TableTreeItem(name, this))
			                               .map(TreeItem::new)
			                               .toList());

			tItems.getChildren().addAll(adb.views()
			                               .map(View::name)
			                               .map(name -> (CustomTreeItem) new ViewTreeItem(name, this))
			                               .map(TreeItem::new)
			                               .toList());

			mainTreeItem.getChildren().add(tItems);
		});

	}


	/**
	 * Open the main database
	 */
	@FXML
	private void openDB() {
		if (db != null && (!db.isClosed())) {
			db.close();
		}

		while (true) {
			File file = openSQLLiteDB();

			if (file == null)
				break;

			try {
				db = Database.open(file.getPath());
				refreshView();
				break;
			} catch (SQLException e) {

				ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
				ButtonType buttonRetry = new ButtonType("Retry");
				ButtonType buttonNewFile = new ButtonType("Choose new");
				ButtonType res = Popup.warning("Problem while opening database", "Error: " + e.getMessage(),
						buttonCancel, buttonRetry, buttonNewFile);

				if (res == buttonNewFile)
					file = openSQLLiteDB();
				else if (res == buttonCancel)
					break;
			}
		}

		databaseMenu.setDisable(false);
	}


	/**
	 * Attach a database to the main database
	 */
	@FXML
	private void attachButton() throws SQLException {

		Stage stage = new Stage();
		Pane attachedChooser;
		AttachedChooserController attachedChooserController = null;

		try {

			FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/attached/AttachedChooser.fxml"));

			stage.setTitle("SQLartan");
			attachedChooser = loader.load();
			attachedChooserController = loader.getController();
			attachedChooserController.setSqlartanController(this);
			stage.initModality(Modality.APPLICATION_MODAL);

			stage.setScene(new Scene(attachedChooser));
			stage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Attach a database to the main database
	 */
	public void attachDatabase(File file, String dbName) {

		try {
			db.attach(file, dbName);

			atachedDBs.add(dbName);

			MenuItem newMenuItem = new MenuItem(dbName);
			newMenuItem.setOnAction(event -> {
				try {
					db.detach(newMenuItem.getText());
					detatchMenu.getItems().removeAll(newMenuItem);
					refreshView();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});

			detatchMenu.getItems().add(newMenuItem);

			refreshView();


		} catch (SQLException e) {

			Alert alert = new Alert(Alert.AlertType.ERROR);
			alert.setTitle("Problem while attatching database");
			alert.setHeaderText(null);
			alert.setContentText(e.getMessage());
			alert.show();
		}


	}


	/**
	 * Open a dialog for the file choos db
	 *
	 * @return the opend database
	 */
	@FXML
	private File openSQLLiteDB() {
		// Create the file popup
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open SQLite database");
		File file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());
		Database tmpDB = null;

		return file;
	}


	/**
	 * Close the current database
	 */
	@FXML
	private void closeDB() throws SQLException {
		mainTreeItem.getChildren().clear();
		stackPane.getChildren().clear();
		db.close();
		databaseMenu.setDisable(true);
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
	public void truncateTable(Table table) throws SQLException {
		table.truncate();
		refreshView();
	}


	/**
	 * Drop a table
	 *
	 * @param table
	 * @throws SQLException
	 */
	public void dropTable(Table table) throws SQLException {
		table.drop();
		refreshView();
	}


	/**
	 * Duplicate a table
	 *
	 * @param table
	 * @throws SQLException
	 */
	public void duplicateTable(Table table, String name) throws SQLException {
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
	public void renameTable(Table table, String name) throws SQLException {
		table.rename(name);
		refreshView();
	}


	/**
	 * Vacuum a database
	 *
	 * @throws SQLException
	 */
	public void vacuumDatabase() throws SQLException {
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
	public void addColumn(Table table, String name, String type) throws SQLException {
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
	public void dropColumn(Table table, String name) throws SQLException {
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
	public void renameColumn(Table table, String name, String newName) throws SQLException {
		if (table.column(name).isPresent()) {
			table.column(name).get().rename(newName);
		}
		refreshView();
	}

	/**
	 * Detach a database from the main database
	 *
	 * @param database
	 * @throws SQLException
	 */
	public void detachDatabase(Database database) throws SQLException {
		db.detach(database.name());
		refreshView();
	}

}
