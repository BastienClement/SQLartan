package sqlartan.view;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.TableColumn;
import sqlartan.core.alterTable.AlterTable;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.util.UncheckedException;
import sqlartan.view.attached.AttachedChooserController;
import sqlartan.view.tabs.DatabaseTabsController;
import sqlartan.view.tabs.TableTabsController;
import sqlartan.view.tabs.ViewTabsController;
import sqlartan.view.treeitem.*;
import sqlartan.view.util.Popup;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
	private Database database = null;
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
	private Button reloadButton;


	// TextArea for the request history
	@FXML
	private ListView<String> request;
	private ObservableList<String> requests = FXCollections.observableArrayList();
	@FXML
	private TitledPane historyPane;
	private CheckBox displayPragma = new CheckBox("PRAGMA");

	// TablePanes
	private TabPane databaseTabPane;
	private TabPane tableTabPane;
	private TabPane viewTabPane;

	// TabsPaneController
	private DatabaseTabsController databaseTabsController;
	private TableTabsController tableTabController;
	private ViewTabsController viewTabsController;

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
	private void initialize() throws IOException {

		treeView.setCellFactory(param -> new CustomTreeCell(this));

		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/DatabaseTabs.fxml"));
		databaseTabPane = loader.load();
		databaseTabsController = loader.getController();

		loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/TableTabs.fxml"));
		tableTabPane = loader.load();
		tableTabController = loader.getController();

		loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/ViewTabs.fxml"));
		viewTabPane = loader.load();
		viewTabsController = loader.getController();

		databaseTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		databaseTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		tableTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		tableTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		viewTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		viewTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

			Optional<? extends PersistentStructure<?>> structure = Optional.empty();
			stackPane.getChildren().clear();

			if (newValue != null) {
				switch (newValue.getValue().type()) {
					case DATABASE: {
						stackPane.getChildren().add(databaseTabPane);
						databaseTabsController.setDatabase(database);
					}
					break;
					case TABLE: {
						structure = database.table(newValue.getValue().name());
						stackPane.getChildren().add(tableTabPane);
						database.table(newValue.getValue().name()).ifPresent(t -> tableTabController.setTable(t));
						structure.ifPresent(tableTabController::setStructure);
						tableTabController.refresh();
					}
					break;
					case VIEW: {
						structure = database.view(newValue.getValue().name());
						stackPane.getChildren().add(viewTabPane);
						structure.ifPresent(viewTabsController::setStructure);
					}
				}
			}
		});

		mainTreeItem = new TreeItem<>(); // Hidden fake root
		mainTreeItem.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setRoot(mainTreeItem);


		// Pane for request history
		BorderPane borderPane = new BorderPane();
		Button clearHistory = new Button("Clear");
		clearHistory.setOnMouseClicked(event -> requests.clear());

		displayPragma.setSelected(true);

		displayPragma.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
		clearHistory.setFocusTraversable(false);
		HBox leftPane = new HBox();
		HBox rightPane = new HBox();
		rightPane.setAlignment(Pos.CENTER);
		leftPane.setAlignment(Pos.CENTER);
		leftPane.setSpacing(15);
		leftPane.getChildren().addAll(displayPragma, clearHistory);
		rightPane.getChildren().add(new Label("History"));
		borderPane.setLeft(rightPane);
		borderPane.setRight(leftPane);
		borderPane.prefWidthProperty().bind(historyPane.widthProperty().subtract(38));

		historyPane.setGraphic(borderPane);

		//Reload button
		ImageView reload = new ImageView(new Image(Sqlartan.class.getResourceAsStream("assets/reload.png")));
		reload.setPreserveRatio(false);
		reload.setFitHeight(10);
		reload.setFitWidth(10);
		reloadButton.prefHeightProperty().bind(reload.yProperty().add(20));
		reloadButton.prefWidthProperty().bind(reload.xProperty().add(35));
		reloadButton.setGraphic(reload);
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

		if (database != null && (!database.isClosed())) {
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

		try {

			FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/attached/AttachedChooser.fxml"));

			stage.setTitle("SQLartan");
			attachedChooser = loader.load();
			AttachedChooserController attachedChooserController = loader.getController();
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
		database.close();
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
	 * Import in the current open database
	 */
	@FXML
	public void importFX() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import SQLite database");
		try {
			File f = fileChooser.showOpenDialog(sqlartan.getPrimaryStage());
			if (f != null) {
				database.importfromFile(f);
			}
		} catch (SQLException | IOException | TokenizeException e) {
			throw new UncheckedException(e);
		}
		refreshView();
	}

	/**
	 * Function called by the GUI
	 * to display the about window
	 */
	@FXML
	private void displayAbout() {
		Stage stage = new Stage();
		Pane pane;

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
	 * To call to refresh the view of the tree
	 */
	@FXML
	public void refreshView() {
		if (database != null) {
			boolean[] exp = new boolean[mainTreeItem.getChildren().size()];
			for (int i = 0; i < exp.length; ++i) {
				exp[i] = mainTreeItem.getChildren().get(i).isExpanded();
			}

			mainTreeItem.getChildren().clear();
			tree(database);

			for (int i = 0; i < exp.length && i < mainTreeItem.getChildren().size(); ++i) {
				mainTreeItem.getChildren().get(i).setExpanded(exp[i]);
			}

			treeView.getSelectionModel().select(0);
		}
	}


	/**
	 * Get the main database
	 *
	 * @return the main database
	 */
	public Database database() {
		return database;
	}


	/**
	 * Open de main database
	 *
	 * @param file: file of the database to open
	 */
	private void openDatabase(File file) {
		if (database != null && (!database.isClosed()))
			database.close();

		try {
			if (file != null) {
				database = Database.open(file);

				request.setCellFactory(e -> setCellFactoryHistory());

				database.registerListener(readOnlyResult -> {
					request.setItems(requests);

					String resultat = readOnlyResult.query();

					if (!resultat.startsWith("PRAGMA") || displayPragma.isSelected())
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
	 * @return the new listcell
	 */
	private ListCell<String> setCellFactoryHistory() {
		ListCell<String> cells = new ListCell<>();
		cells.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY)) {
				if (event.getClickCount() == 2) {
					String request = cells.itemProperty().getValue();
					treeView.getSelectionModel().select(0);
					databaseTabsController.selectSqlTab();
					databaseTabsController.setSqlRequest(request);
				}
			}
		});
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
	 * @param file   file of the database
	 * @param dbName name that will be shown in the treeView
	 */
	public void attachDatabase(File file, String dbName) {

		try {
			database.attach(file, dbName);

			atachedDBs.add(dbName);

			MenuItem newMenuItem = new MenuItem(dbName);
			newMenuItem.setOnAction(event -> {
				database.detach(newMenuItem.getText());
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
	 * Drop a table or a view
	 *
	 * @param structure structure to drop
	 */
	public void dropStructure(PersistentStructure<?> structure) {
		structure.drop();
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
		//refreshView();
	}


	/**
	 * Add a table to the specified database
	 *
	 * @param name
	 */
	public void addTable(Database db, String name) {
		try {
			db.addTable(name);
			refreshView();
		} catch (SQLException e) {
			throw new UncheckedException(e);
		}
	}

	/**
	 * Add a column to the specified table
	 *
	 * @param table
	 * @param name
	 * @param type
	 */
	public void addColumn(Table table, String name, String type, boolean unique, boolean primaryKey, boolean nullable) {
		TableColumn column = new TableColumn(table, new TableColumn.Properties() {
			@Override
			public boolean unique() {
				return unique;
			}
			@Override
			public boolean primaryKey() {
				return primaryKey;
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
				return nullable;
			}
		});
		AlterTable alter = table.alter();
		alter.addColumn(column);
		alter.execute();
		refreshView();
	}


	/**
	 * Rename the specified column from the table
	 *
	 * @param structure
	 * @param newName
	 */
	public void renameColumn(PersistentStructure<? extends TableColumn> structure, String name, String newName) {
		structure.column(name).ifPresent(c -> c.rename(newName));
		refreshView();
	}


	/**
	 * Detach a database from the main database
	 *
	 * @param database
	 */
	public void detachDatabase(Database database) {
		this.database.detach(database.name());
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


	@FXML
	public void export() {
		class Result{
			private boolean structure, data, structureAndData;

			public Result(boolean structure, boolean data, boolean structureAndData) {
				this.structure = structure;
				this.data = data;
				this.structureAndData = structureAndData;
			}
		}

		// Create the custom dialog.
		Dialog<Result> dialog = new Dialog<>();
		dialog.setTitle("Choose option for export");
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		// Create the two labels and fields
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		final ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton("Structure");
		rb1.setToggleGroup(group);
		rb1.setSelected(true);

		RadioButton rb2 = new RadioButton("Data");
		rb2.setToggleGroup(group);

		RadioButton rb3 = new RadioButton("Structure and data");
		rb3.setToggleGroup(group);

		grid.add(new Label("Choose one option : "), 0, 0);
		grid.add(rb1, 0, 1);
		grid.add(rb2, 0, 2);
		grid.add(rb3, 0, 3);

		dialog.getDialogPane().setContent(grid);

		// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				Boolean wtf = rb1.isSelected();
				return new Result(rb1.isSelected(), rb2.isSelected(), rb3.isSelected());
			}
			return null;
		});

		dialog.showAndWait().ifPresent(result -> {
			FileChooser fileChooser = new FileChooser();

			//Set extension filter
			FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("SQL files (*.sql)", "*.sql");
			fileChooser.getExtensionFilters().add(extFilter);

			try {
				//Show save file dialog
				File file = fileChooser.showSaveDialog(sqlartan.getPrimaryStage());
				if(file != null){
					FileWriter fileWriter = new FileWriter(file);
					if(result.structure){
						fileWriter.write(database.exportStructure());
					} else if (result.data){
						fileWriter.write(database.exportTablesData());
					} else if (result.structureAndData){
						fileWriter.write(database.export());
					}
					fileWriter.close();
				}
			} catch (IOException | SQLException e) {
				throw new UncheckedException(e);
			}
		});
	}


}
