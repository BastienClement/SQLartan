package sqlartan.gui.controller;

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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.TableColumn;
import sqlartan.core.alter.AlterTable;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.gui.controller.tabs.DatabaseTabsController;
import sqlartan.gui.controller.tabs.TableTabsController;
import sqlartan.gui.controller.tabs.ViewTabsController;
import sqlartan.gui.controller.treeitem.*;
import sqlartan.gui.util.Popup;
import sqlartan.util.UncheckedException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.stream.Collectors;
import static sqlartan.util.Matching.match;

/**
 * Controller for the Sqlartan.fxml.
 */
public class SqlartanController {


	private final String WARNING_COST = " operation is a emulated operation, it can be expensive if the table contains a large volume of data";
	/**************
	 * ATTRIBUTES *
	 **************/
	private Database database;
	private TreeItem<CustomTreeItem> mainTreeItem;
	private Sqlartan sqlartan;
	@FXML
	private TreeView<CustomTreeItem> treeView;
	@FXML
	private StackPane stackPane;
	@FXML
	private Menu detachMenu;
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


	/*****************************
	 * METHODS called by the GUI*
	 *****************************/
	/**
	 * First method call when FXML loaded
	 * Load all the tabs and create the history pane
	 */
	@FXML
	private void initialize() throws IOException {

		treeView.setCellFactory(param -> new CustomTreeCell());

		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("gui/view/DatabaseTabs.fxml"));
		databaseTabPane = loader.load();
		databaseTabsController = loader.getController();

		loader = new FXMLLoader(Sqlartan.class.getResource("gui/view/TableTabs.fxml"));
		tableTabPane = loader.load();
		tableTabController = loader.getController();

		loader = new FXMLLoader(Sqlartan.class.getResource("gui/view/ViewTabs.fxml"));
		viewTabPane = loader.load();
		viewTabsController = loader.getController();

		databaseTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		databaseTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		tableTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		tableTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		viewTabPane.prefHeightProperty().bind(stackPane.heightProperty());
		viewTabPane.prefWidthProperty().bind(stackPane.widthProperty());

		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue != null) {
				stackPane.getChildren().clear();
				switch (newValue.getValue().type()) {
					case DATABASE:
						stackPane.getChildren().add(databaseTabPane);
						databaseTabsController.setDatabase(newValue.getValue().database());
						databaseTabsController.refresh();
						break;
					case TABLE:
						stackPane.getChildren().add(tableTabPane);
						newValue.getValue().database().table(newValue.getValue().name()).ifPresent(tableTabController::setStructure);
						tableTabController.refresh();
						break;
					case VIEW:
						stackPane.getChildren().add(viewTabPane);
						newValue.getValue().database().view(newValue.getValue().name()).ifPresent(viewTabsController::setStructure);
						viewTabsController.refresh();
						break;
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
	protected void createDatabase() {
		Popup.save("Create a new database", sqlartan.getPrimaryStage(), null)
		     .ifPresent(file -> {
			     if (database != null && (!database.isClosed())) {
				     attachDatabase(file, file.getName().split("\\.")[0]);
			     } else {
				     openDatabase(file);
			     }
		     });
	}


	/**
	 * FUnction called by the GUI
	 * to attache a database
	 */
	@FXML
	protected void attachButton() {

		Stage stage = new Stage();
		Pane attachedChooser;

		try {

			FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("gui/view/AttachedChooser.fxml"));

			stage.setTitle("SQLartan");
			attachedChooser = loader.load();
			AttachedChooserController attachedChooserController = loader.getController();
			attachedChooserController.setController(this);
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
	protected void closeDatabase() {
		mainTreeItem.getChildren().clear();
		stackPane.getChildren().clear();
		detachMenu.getItems().clear();
		database.close();
		databaseMenu.setDisable(true);
	}


	/**
	 * Close the entry application
	 */
	@FXML
	protected void close() {
		Platform.exit();
	}


	/**
	 * Import in the current open database
	 */
	@FXML
	public void importDatabase(Database database) {
		Popup.browse("Import SQLite database", sqlartan.getPrimaryStage(), null)
		     .ifPresent(file -> {
			     try {
				     database.importFromFile(file);
				     refreshView();
			     } catch (SQLException | IOException | TokenizeException e) {
				     throw new UncheckedException(e);
			     }
		     });
	}

	/**
	 * Function called by the GUI
	 * to display the about window
	 */
	@FXML
	protected void displayAbout() {
		Stage stage = new Stage();
		Pane pane;

		try {
			FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("gui/view/About.fxml"));

			stage.setTitle("SQLartan - About");
			pane = loader.load();
			stage.initModality(Modality.APPLICATION_MODAL);
			stage.setResizable(false);

			stage.setScene(new Scene(pane));
			stage.showAndWait();

		} catch (IOException e) {
			throw new UncheckedException(e);
		}
	}


	/**
	 * To call to refresh the gui of the tree
	 */
	@FXML
	public void refreshView() {
		if (database != null) {
			int selected = treeView.getSelectionModel().getSelectedIndex();

			boolean[] exp = new boolean[mainTreeItem.getChildren().size()];
			for (int i = 0; i < exp.length; ++i) {
				exp[i] = mainTreeItem.getChildren().get(i).isExpanded();
			}

			mainTreeItem.getChildren().clear();
			tree(database);

			for (int i = 0; i < exp.length && i < mainTreeItem.getChildren().size(); ++i) {
				mainTreeItem.getChildren().get(i).setExpanded(exp[i]);
			}


			treeView.getSelectionModel().select(selected);
			refreshAttachedDatabase();
		}
	}

	/**
	 * Refresh de attached database menu
	 */
	private void refreshAttachedDatabase() {
		detachMenu.getItems().clear();
		detachMenu.getItems().addAll(
			database.attached().values().stream()
			        .map(AttachedDatabase::name)
			        .map(MenuItem::new)
			        .peek(item -> item.setOnAction(event -> database.attached(item.getText()).ifPresent(this::detachDatabase)))
			        .collect(Collectors.toList()));
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
	 * Open a popup with à FileChooser, asking which database to open
	 */
	public void openDatabase() {
		Popup.browse("Open SQLite database", sqlartan.getPrimaryStage(), null).ifPresent(this::openDatabase);
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

					String result = readOnlyResult.query();

					if (!result.startsWith("PRAGMA") || displayPragma.isSelected())
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
			     .ifPresent(b -> openDatabase());
		}
	}


	/**
	 * CellFactory for the history listedView
	 *
	 * @return the new ListCell
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
	 * @param sqlartan set the reference to the main class
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
		TreeItem<CustomTreeItem> trees = new TreeItem<>(new DatabaseTreeItem(database.name(), this, database));

		trees.getChildren().addAll(database.structures()
		                                   .map(structure -> match(structure, CustomTreeItem.class)
			                                   .when(Table.class, t -> new TableTreeItem(t.name(), this, database))
			                                   .when(View.class, v -> new ViewTreeItem(v.name(), this, database))
			                                   .orElseThrow())
		                                   .map(TreeItem::new)
		                                   .toList());

		mainTreeItem.getChildren().add(trees);

		// Attached database
		database.attached().values().forEach(adb -> {
			TreeItem<CustomTreeItem> tItems = new TreeItem<>(new AttachedDatabaseTreeItem(adb.name(), this, adb));
			tItems.getChildren().addAll(
				adb.structures().map(structure -> match(structure, CustomTreeItem.class)
					.when(Table.class, t -> new TableTreeItem(t.name(), this, adb))
					.when(View.class, v -> new ViewTreeItem(v.name(), this, adb))
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
			refreshView();
		} catch (SQLException e) {
			Popup.error("Problem while attaching database", e.getMessage());
		}
	}


	/**
	 * Drop a table or a gui
	 *
	 * @param structure structure to drop
	 */
	public void dropStructure(PersistentStructure<?> structure) {
		ButtonType yes = new ButtonType("Yes");
		ButtonType no = new ButtonType("No");
		Popup.warning("Drop " + structure.name(), "Are you sure to drop the " + structure.name(), yes, no)
		     .filter(b -> b == yes)
		     .ifPresent(b -> {
			     structure.drop();
			     refreshView();
		     });
	}


	/**
	 * Ask the user the new name of the structure
	 *
	 * @param structure the structure to rename
	 */
	public void renameStructure(PersistentStructure<?> structure) {
		Popup.input("Rename", "Rename " + structure.name() + " into :", structure.name()).ifPresent(name -> {
			if (name.length() > 0 && !structure.name().equals(name)) {
				structure.rename(name);
				refreshView();
			} else {
				Popup.error("Rename error", "The name is already used or don't have enough chars");
			}
		});
	}


	/**
	 * Add a table to the specified database
	 *
	 * @param database the database in where the datable will be added
	 */
	public void addTable(Database database) {
		Popup.input("Add table", "Name : ", "").ifPresent(name -> {
			if (name.length() > 0) {
				try {
					database.createTable(name);
					refreshView();
				} catch (SQLException e) {
					throw new UncheckedException(e);
				}
			}
		});
	}

	/**
	 * Ask the user to specified the column to add, and add it
	 *
	 * @param table the table where adding a column
	 */
	public void addColumn(Table table) {
		class AddColumnResult {
			private String name, type;
			private boolean unique, primary, nullable;

			private AddColumnResult(String name, String type, boolean unique, boolean primary, boolean nullable) {
				this.name = name;
				this.type = type;
				//this.check = check == "" ? null : check;
				this.unique = unique;
				this.primary = primary;
				this.nullable = nullable;
			}
		}

		// Create the custom dialog.
		Dialog<AddColumnResult> dialog = new Dialog<>();
		dialog.setTitle("Add column");
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		// Create the two labels and fields
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField name = new TextField();

		ChoiceBox type = new ChoiceBox<>(FXCollections.observableArrayList("TEXT", "INTEGER", "NULL", "REAL", "BLOB"));
		type.getSelectionModel().selectFirst();

		CheckBox unique = new CheckBox();
		CheckBox primary = new CheckBox();
		CheckBox nullable = new CheckBox();

		//TextField check = new TextField();

		grid.add(new Label("Name : "), 0, 0);
		grid.add(name, 1, 0);
		grid.add(new Label("Type : "), 0, 1);
		grid.add(type, 1, 1);
		grid.add(new Label("Unique : "), 0, 2);
		grid.add(unique, 1, 2);
		grid.add(new Label("Primary : "), 0, 3);
		grid.add(primary, 1, 3);
		grid.add(new Label("Nullable : "), 0, 4);
		grid.add(nullable, 1, 4);
		//grid.add(new Label("Check : "), 0, 5);
		//grid.add(check, 1, 5);

		dialog.getDialogPane().setContent(grid);


		// Convert the result to a username-password-pair when the button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				return new AddColumnResult(name.getText(), type.getValue().toString(), unique.isSelected(), primary.isSelected(), nullable.isSelected());
			}
			return null;
		});

		dialog.showAndWait().ifPresent(addColumnResult -> {
			AlterTable alter = table.alter();
			alter.addColumn(new TableColumn(table, new TableColumn.Properties() {
				@Override
				public boolean unique() {
					return addColumnResult.unique;
				}
				@Override
				public boolean primaryKey() {
					return addColumnResult.primary;
				}
				@Override
				public String check() {
					return null;
				}
				@Override
				public String name() {
					return addColumnResult.name;
				}
				@Override
				public String type() {
					return addColumnResult.type;
				}
				@Override
				public boolean nullable() {
					return addColumnResult.nullable;
				}
			}));
			alter.execute();
			refreshView();
		});
	}


	/**
	 * Ask the user the new name of the column, if the name is more than 0 characters and isn't the same of the
	 * structure, rename the specified column from the structure
	 *
	 * @param structure  the structure where the column is
	 * @param columnName the name of the column
	 */
	public void renameColumn(PersistentStructure<? extends TableColumn> structure, String columnName) {
		Popup.input("Rename " + structure.name(), "Rename " + structure.name() + " into : ", structure.name(), "Column rename" + WARNING_COST)
		     .ifPresent(newName -> {
			     if (newName.length() > 0 && !structure.name().equals(newName)) {
				     structure.column(columnName).ifPresent(c -> c.rename(newName));
				     refreshView();
			     }
		     });
	}


	/**
	 * Ask the user if he's sure to drop the column of a structure, if it's yes, drop the column.
	 *
	 * @param structure  the structure where the column is
	 * @param columnName the name of the column
	 */
	public void dropColumn(PersistentStructure<? extends TableColumn> structure, String columnName) {
		structure.column(columnName).ifPresent(c -> {
			ButtonType yes = new ButtonType("Yes");
			ButtonType no = new ButtonType("No");
			Popup.warning("Drop " + columnName, "The drop column" + WARNING_COST, "Are you sure to drop the column " +
				columnName + " of " + structure.name(), yes, no)
			     .filter(b -> b == yes)
			     .ifPresent(b -> {
				     c.drop();
				     refreshView();
			     });
		});
	}

	/**
	 * Detach a attachedDatabase from the main attachedDatabase
	 *
	 * @param attachedDatabase the attached database to detach
	 */
	public void detachDatabase(AttachedDatabase attachedDatabase) {
		database.detach(attachedDatabase.name());
		refreshView();
	}


	/**
	 * Set active the index element in the TreeView
	 *
	 * @param index to set active
	 */
	public void selectTreeIndex(int index) {
		treeView.getSelectionModel().select(index);
	}


	/**
	 * Export the database, ask the user what it would like to export (structure, data, or both)
	 *
	 * @param database the database to export
	 */
	@FXML
	public void export(Database database) {
		class Result {
			private boolean structure, data, structureAndData;

			private Result(boolean structure, boolean data, boolean structureAndData) {
				this.structure = structure;
				this.data = data;
				this.structureAndData = structureAndData;
			}
		}

		// Create the custom dialog.
		Dialog<Result> dialog = new Dialog<>();
		dialog.setTitle("Exporting " + database.name());
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
				return new Result(rb1.isSelected(), rb2.isSelected(), rb3.isSelected());
			}
			return null;
		});

		dialog.showAndWait().ifPresent(result -> {
			//Show save file dialog
			Popup.save("Export", sqlartan.getPrimaryStage(), new FileChooser.ExtensionFilter("SQL files (*.sql)", "*.sql"))
			     .ifPresent(file -> {
				     try {
					     FileWriter fileWriter = new FileWriter(file);
					     if (result.structure) {
						     fileWriter.write(database.exportStructure());
					     } else if (result.data) {
						     fileWriter.write(database.exportTablesData());
					     } else if (result.structureAndData) {
						     fileWriter.write(database.export());
					     }
					     fileWriter.close();
				     } catch (IOException | SQLException e) {
					     throw new UncheckedException(e);
				     }
			     });
		});
	}


	/**
	 * Vacuum the database and inform the user with a popup
	 *
	 * @param database the database to vacuum
	 */
	public void vacuum(Database database) {
		database.vacuum();
		Popup.information("Vacuum", "The database " + database.name() + " get vacuumed");
	}


	/**
	 * Ask the user the name of the duplicate structure
	 *
	 * @param structure the structure to duplicate
	 */
	public void duplicate(PersistentStructure<?> structure) {
		Popup.input("Duplicate", "Name : ", structure.name()).ifPresent(name -> {
			if (name.length() > 0 && !structure.name().equals(name)) {
				structure.duplicate(name);
				refreshView();
			} else {
				Popup.error("Duplicate error", "The name is already used or don't have enough chars");
			}
		});
	}


	/**
	 * Ask the user if it would like to truncate the database, if it's yes, truncate the table
	 *
	 * @param table the table to truncate
	 */
	public void truncate(Table table) {
		ButtonType yes = new ButtonType("Yes");
		ButtonType no = new ButtonType("No");
		Popup.warning("Truncate " + table.name(), "Are you sure to truncate " + table.name(), yes, no)
		     .filter(b -> b == yes)
		     .ifPresent(b -> {
			     table.truncate();
			     refreshView();
		     });
	}
}
