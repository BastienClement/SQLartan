package sqlartan.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.TableColumn;
import sqlartan.core.alterTable.AlterTable;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.util.UncheckedException;
import sqlartan.view.attached.AttachedChooserController;
import sqlartan.view.tabs.DatabaseTabsController;
import sqlartan.view.tabs.TableTabsController;
import sqlartan.view.treeitem.*;
import sqlartan.view.util.Popup;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import static sqlartan.util.Matching.match;

/**
 * SqlartanController
 */
public class SqlartanController {


	/***********
	 * ATRIBUTS*
	 ***********/
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

	private DatabaseTabsController databaseTabsController;


	// TextArea for the request history
	@FXML
	private ListView<String> request;
	private ObservableList<String> requests = FXCollections.observableArrayList();
	@FXML
	private TitledPane historyPane;
	private CheckBox displayPragma = new CheckBox("PRAGMA");

	@FXML
	private Menu databaseMenu;
	private List<String> atachedDBs = new LinkedList<>();


	/*****************************
	 * METHODES called by the GUI*
	 *****************************/


	/**
	 * First methode call when FXML loaded
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

						databaseTabsController = loader.getController();
						databaseTabsController.setDatabase(db);
						databaseTabsController.setController(this);
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
						tabsController.setDatabase(db);
						tabsController.setController(this);
						tabsController.setTable(db.table(treeItem.name()).get());
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


		// Pane for request history
		BorderPane borderPane = new BorderPane();
		Button clearHistory = new Button("Clear");
		clearHistory.setOnMouseClicked(event -> {
			requests.clear();
		});

		displayPragma.setSelected(true);

		displayPragma.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		HBox leftPane = new HBox();
		HBox rightPane = new HBox();
		rightPane.setAlignment(Pos.CENTER);
		leftPane.setAlignment(Pos.CENTER);
		leftPane.setSpacing(5);
		leftPane.getChildren().addAll(displayPragma, clearHistory);
		rightPane.getChildren().add(new Label("History"));
		borderPane.setLeft(rightPane);
		borderPane.setRight(leftPane);
		borderPane.prefWidthProperty().bind(historyPane.widthProperty().subtract(38));

		historyPane.setGraphic(borderPane);

	}


	/**
	 * Function called by the GUI
	 * to create a new database and open or attache it
	 */
	@FXML
	private void createDatabase() throws SQLException {
		FileChooser fileChooser = new FileChooser();

		//Show save file dialog
		fileChooser.setTitle("Create a new database");
		File file = fileChooser.showSaveDialog(sqlartan.getPrimaryStage());

		if (db != null && (!db.isClosed())) {
			attachDatabase(file, file.getName().split("\\.")[0]);
		} else {
			openDatabase(file);
		}
	}
	

	/**
	 * Function called by the GUI
	 * to open a database
	 */
	@FXML
	private void openSQLiteDatabase() {
		// Create the file popup
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open SQLite database");

		File file = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());

		openDatabase(file);
	}


	/**
	 * FUnction called by the GUI
	 * to attache a database
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
	 * Import in the current opend database
	 */
	@FXML
	public void importFX() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import SQLite database");
		try {
			File f = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());
			if (f != null) {
				db.importfromFile(f);
			}
		} catch (SQLException | IOException | TokenizeException e) {
			throw new UncheckedException(e);
		}
		refreshView();
	}


	/**
	 * Export the database
	 *
	 * @throws SQLException
	 */
	@FXML
	public void export() {
		FileChooser fileChooser = new FileChooser();

		//Set extension filter
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SQL files (*.sql)", "*.sql");
		fileChooser.getExtensionFilters().add(extFilter);

		try {
			//Show save file dialog
			File file = fileChooser.showSaveDialog(sqlartan.getPrimaryStage());
			if (file != null) {
				FileWriter fileWriter = new FileWriter(file);
				fileWriter.write(db.export());
				fileWriter.close();
			}
		} catch (IOException | SQLException e) {
			throw new UncheckedException(e);
		}
	}


	/**
	 * Function called by the GUI
	 * to display the about window
	 */
	@FXML
	private void displayAbout() {
		Stage stage = new Stage();
		Pane pane;
		AttachedChooserController attachedChooserController = null;

		try {
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


	/**
	 * Get the main database
	 *
	 * @return the main database
	 */
	public Database getDB() {
		return db;
	}


	/**
	 * Open de main database
	 *
	 * @param file: file of the database to open
	 */
	private void openDatabase(File file) {
		if (db != null && (!db.isClosed()))
			db.close();

		try {
			if (file != null) {
				db = Database.open(file);

				request.setCellFactory(e -> setCellFactoryHistory());

				db.registerListener(readOnlyResult -> {
					request.setItems(requests);

					String resultat = readOnlyResult.query();

					if (resultat.startsWith("PRAGMA") && !displayPragma.isSelected())
						return;

					requests.add(0, readOnlyResult.query());
				});

				databaseMenu.setDisable(false);
				refreshView();
			}
		} catch (SQLException e) {
			ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
			ButtonType buttonRetry = new ButtonType("Retry");
			Popup.warning("Problem while opening database", "Error: " + e.getMessage(), buttonCancel, buttonRetry)
			     .filter(b -> buttonRetry == b)
			     .ifPresent(b -> openSQLiteDatabase());
		}
	}


	/**
	 * CellFactory for the history listedView
	 *
	 * @return the new ListCell
	 */
	private ListCell<String> setCellFactoryHistory(){
		ListCell<String> cells = new ListCell<>();
		ContextMenu menu = new ContextMenu();
		MenuItem execute = new MenuItem();

		execute.textProperty().bind(Bindings.format("Execute \"%s\" ", cells.itemProperty()));
		execute.setOnAction(event -> {
			String request = cells.itemProperty().getValue();
			treeView.getSelectionModel().select(0);
			databaseTabsController.selectSqlTab();
			databaseTabsController.setSqlRequest(request);
		});

		menu.getItems().add(execute);

		cells.textProperty().bind(cells.itemProperty());

		cells.emptyProperty().addListener((obs, wasEmpty, isNotEmpty) -> {
			cells.setContextMenu(isNotEmpty ? null : menu);
		});

		return cells;
	}


	/**
	 * Called by the mainApp to set the link to the mainApp
	 *
	 * @param sqlartan set the referance to the main class
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
	private void tree(Database database) {

		// Main
		TreeItem<CustomTreeItem> trees = new TreeItem<>(new DatabaseTreeItem(database.name(), this));

		trees.getChildren().addAll(database.structures()
		                                   .map(structure -> match(structure, CustomTreeItem.class)
			                                   .when(Table.class, t -> new TableTreeItem(t.name(), this))
			                                   .when(View.class, v -> new ViewTreeItem(v.name(), this))
			                                   .orElseThrow())
		                                   .map(TreeItem::new)
		                                   .toList());

		mainTreeItem.getChildren().add(trees);

		// Attached database
		database.attached().values().forEach(adb -> {
			TreeItem<CustomTreeItem> tItems = new TreeItem<>(new AttachedDatabaseTreeItem(adb.name(), this));
			tItems.getChildren().addAll(
				adb.structures().map(structure -> match(structure, CustomTreeItem.class)
					.when(Table.class, t -> new TableTreeItem(t.name(), this))
					.when(View.class, v -> new ViewTreeItem(v.name(), this))
					.orElseThrow())
				   .map(TreeItem::new)
				   .toList());

			mainTreeItem.getChildren().add(tItems);
		});
	}


	/**
	 * Attach a database to the main database
	 *
	 * @param file   : file of the database
	 * @param dbName : name that will be shown in the treeView
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
			Popup.error("Problem while attaching database", e.getMessage());
		}


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
	 * Drop a table or a view
	 *
	 * @param structure
	 */
	public void dropStructure(PersistentStructure<?> structure) {
		structure.drop();
		refreshView();
	}


	/**
	 * Duplicate a table
	 *
	 * @param structure
	 */
	public void duplicateStructure(PersistentStructure<?> structure, String name) {
		structure.duplicate(name);
		refreshView();
	}


	/**
	 * Rename a table or a view
	 *
	 * @param structure
	 * @param name
	 */
	public void renameStructure(PersistentStructure<?> structure, String name) {
		structure.rename(name);
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
		TableColumn column = new TableColumn(table, new TableColumn.Properties() {
			@Override
			public boolean unique() {
				return false;
			}
			@Override
			public String check() {
				return null;
			}
			@Override
			public String name() {
				return name;
			}
			@Override
			public String type() {
				return type;
			}
			@Override
			public boolean nullable() {
				return false;
			}
		});
		AlterTable alter = table.alter();
		try {
			alter.addColumn(column);
			alter.execute();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		refreshView();
	}


	/**
	 * Drop the specified column from the table
	 *
	 * @param table
	 */
	public void dropColumn(Table table, String name) {
		table.column(name).ifPresent(TableColumn::drop);
		refreshView();
	}


	/**
	 * Rename the specified column from the table
	 *
	 * @param structure
	 * @param newName
	 */
	public void renameColumn(PersistentStructure<?> structure, String name, String newName) {
		throw new RuntimeException("not implemented");
		// structure.column(name).ifPresent(c -> c.rename(newName)); // TODO
		//refreshView();
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
	 * Set active the index element in the treeview
	 *
	 * @param index to set active
	 */
	public void selectTreeIndex(int index) {
		treeView.getSelectionModel().select(index);
	}


}
