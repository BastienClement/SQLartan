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
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.core.TableColumn;
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
	private TreeItem<CustomTreeItem> mainTreeItem;
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
	private void initialize() {

		treeView.setCellFactory(param -> new CustomTreeCell(this));

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
	 */
	void refreshView() {
		if (db != null) {
			boolean[] exp = new boolean[mainTreeItem.getChildren().size()];
			for (int i = 0; i < exp.length; ++i) {
				exp[i] = mainTreeItem.getChildren().get(i).isExpanded();
			}

			mainTreeItem.getChildren().clear();
			tree(db);

			for (int i = 0; i < exp.length && i < mainTreeItem.getChildren().size(); ++i) {
				mainTreeItem.getChildren().get(i).setExpanded(exp[i]);
			}
		}
	}


	/**
	 * Create the tree for a specific database
	 *
	 * @param database the database
	 */
	void tree(Database database) {

		// Main
		TreeItem<CustomTreeItem> trees = new TreeItem<>(new DatabaseTreeItem(database.name(), this));

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
			TreeItem<CustomTreeItem> tItems = new TreeItem<>(new AttachedDatabaseTreeItem(adb.name(), this));
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
				Optional<ButtonType> res = Popup.warning("Problem while opening database", "Error: " + e.getMessage(),
					buttonCancel, buttonRetry, buttonNewFile);
				if (res.isPresent()) {
					if (res.get() == buttonNewFile)
						file = openSQLLiteDB();
					else if (res.get() == buttonCancel)
						break;
				}
			}
		}

		databaseMenu.setDisable(false);
	}


	/**
	 * Attach a database to the main database
	 */
	@FXML
	private void attachButton() {

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
				db.detach(newMenuItem.getText());
				detatchMenu.getItems().removeAll(newMenuItem);
				refreshView();

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
	 * Open a dialog for the file to choose db
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
	private void closeDB() {
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
	 */
	public void truncateTable(Table table) {
		table.truncate();
		refreshView();
	}


	/**
	 * Drop a table
	 *
	 * @param table
	 */
	public void dropTable(Table table) {
		table.drop();
		refreshView();
	}


	/**
	 * Duplicate a table
	 *
	 * @param table
	 */
	public void duplicateTable(Table table, String name) {
		table.duplicate(name);
		refreshView();
	}


	/**
	 * Rename a table
	 *
	 * @param table
	 * @param name
	 */
	public void renameTable(Table table, String name) {
		table.rename(name);
		refreshView();
	}


	/**
	 * Vacuum a database
	 */
	public void vacuumDatabase(Database db) {
		db.vacuum();
		refreshView();
	}


	/**
	 * Add a column to the specified table
	 *
	 * @param table
	 * @param name
	 * @param type
	 */
	public void addColumn(Table table, String name, String type) {
		Affinity affinity = Affinity.forType(type);
		table.addColumn(name, affinity);
		refreshView();
	}


	/**
	 * Drop the specified column from the table
	 *
	 * @param table
	 * @param name
	 */
	public void dropColumn(Table table, String name) {
		table.column(name).ifPresent(TableColumn::drop);
		refreshView();
	}


	/**
	 * Rename the specified column from the table
	 *
	 * @param table
	 * @param name
	 * @param newName
	 */
	public void renameColumn(Table table, String name, String newName) {
		table.column(name).ifPresent(t -> t.rename(newName));
		refreshView();
	}

	/**
	 * Detach a database from the main database
	 *
	 * @param database
	 */
	public void detachDatabase(Database database) {
		db.detach(database.name());
		refreshView();
	}

	/**
	 * Executes the SQL contained in a string into the database
	 *
	 * @param database
	 * @param sql
	 */
	public void importFromString(Database database, String sql) throws SQLException, TokenizeException {
		database.importFromString(sql);
	}

	/**
	 * Export a database
	 *
	 * @param database
	 */
	public String export(Database database) throws SQLException {
		return database.export();
	}

	/**
	 * Display About
	 */
	@FXML
	private void displayAbout(){
		Stage stage = new Stage();
		Pane pane;
		AttachedChooserController attachedChooserController = null;

		try{
			FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/about/About.fxml"));

			stage.setTitle("SQLartan - About");
			pane = loader.load();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);

			stage.setScene(new Scene(pane));
			stage.showAndWait();

	} catch (IOException e) {
		e.printStackTrace();
	}
	}

}
