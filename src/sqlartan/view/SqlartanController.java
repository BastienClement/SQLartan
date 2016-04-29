package sqlartan.view;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.*;
import sqlartan.core.util.RuntimeSQLException;
import sqlartan.utils.Optionals;
import java.io.File;
import java.io.IOException;
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
	private TreeView<DbTreeItem> treeView;

	@FXML
	private BorderPane borderPane;

	@FXML
	private StackPane stackPane;


	private TableView tableView = new TableView();

	TreeItem<DbTreeItem> mainTreeItem;

	File file;

	@FXML
	private void initialize() throws SQLException {
		tableView.setEditable(true);
		tableView.setVisible(true);
		treeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue.getValue().getType() == Type.DATABASE)
			{
				FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/DatabaseTabs.fxml"));

				TabPane tabPane = null;

				try {
					tabPane = loader.load();
				} catch (IOException e) {
					e.printStackTrace();
				}

				tabPane.prefHeightProperty().bind(stackPane.heightProperty());
				tabPane.prefWidthProperty().bind(stackPane.widthProperty());

				stackPane.getChildren().add(tabPane);

			}
			else if (newValue.getValue().getType() == Type.TABLE || newValue.getValue().getType() == Type.VIEW)
			{
				FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/TableTabs.fxml"));

				TabPane tabPane = null;

				try {
					tabPane = loader.load();
				} catch (IOException e) {
					e.printStackTrace();
				}

				tabPane.prefHeightProperty().bind(stackPane.heightProperty());
				tabPane.prefWidthProperty().bind(stackPane.widthProperty());

				stackPane.getChildren().add(tabPane);
			}

			if (newValue != null) {
				Optionals.firstPresent(
						() -> db.table(newValue.getValue().getName()),
						() -> db.view(newValue.getValue().getName())
				).ifPresent(this::dataView);
			}
		});


		mainTreeItem = new TreeItem<>(); // Hidden
		mainTreeItem.setExpanded(true);
		treeView.setShowRoot(false);
		treeView.setRoot(mainTreeItem);
	}

	/**
	 * Called by the mainApp to set the link to the mainApp
	 * @param sqlartan
	 */
	public void setApp(Sqlartan sqlartan) {
		this.sqlartan = sqlartan;
	}

	void dataView(PersistentStructure<?> structure) {
		try {
			//sqlartan.getMainLayout().setCenter(tableView);
			dataView(structure.database().assemble("SELECT * FROM ", structure.name()).execute(), tableView);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
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
/*
		// Attached database
		for (AttachedDatabase adb : database.attached().values()) {
			TreeItem<DbTreeItem> tItems = new TreeItem<>(adb.name());
			tItems.getChildren().addAll(adb.tables()
			                               .map(Table::name)
			                               .map(TreeItem::new)
			                               .toList());

			tItems.getChildren().addAll(adb.views()
			                               .map(View::name)
			                               .map(TreeItem::new)
			                               .toList());

			mainTreeItem.getChildren().add(tItems);
		}*/
	}

	void dataView(Result res, TableView tv) {
		tv.getColumns().clear();
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
			tv.getColumns().addAll(col);
			System.out.println("Column [" + i++ + "] " + c.name());
		}

		/********************************
		 * Data added to ObservableList *
		 ********************************/
		rows.clear();
		res.forEach(row -> rows.add(FXCollections.observableArrayList(
				res.columns().map(c -> row.getString()))
		));
		tv.setItems(rows);

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
				db = Database.open(file.getPath());
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
		db.close();
	}


	/**
	 * Close the entery application
	 */
	@FXML
	private void close()
	{
		Platform.exit();
	}

}
