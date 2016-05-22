package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.util.Callback;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.view.tabs.struct.DatabaseStructure;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by julien on 30.04.16.
 */
public class DatabaseTabsController extends TabsController {

	@FXML
	private TableColumn<DatabaseStructure, String> colName;
	@FXML
	private TableColumn<DatabaseStructure, String> colType;
	@FXML
	private TableColumn<DatabaseStructure, String> colLignes;
	@FXML
	private TableColumn<DatabaseStructure, String> colRename;
	@FXML
	private TableColumn<DatabaseStructure, String> colDelete;
	@FXML
	private TabPane tabPane;

	@FXML
	private Tab structureTab;

	@FXML
	private TableView<DatabaseStructure> structureTable;

	private Database database;

	private ObservableList<DatabaseStructure> dbStructs = FXCollections.observableArrayList();

	@FXML
	private Pane sqlTab;

	@FXML
	private void initialize() {
		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == structureTab) {
				displayStructure();
			}
		});

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colLignes.setCellValueFactory(param -> param.getValue().lignesProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());
		colRename.setCellFactory(new Callback<TableColumn<DatabaseStructure, String>, TableCell<DatabaseStructure, String>>() {
			@Override
			public TableCell call( final TableColumn<DatabaseStructure, String> param )
			{
				final TableCell<DatabaseStructure, String> cell = new TableCell<DatabaseStructure, String>()
				{
					final Button btn = new Button( "Rename" );

					@Override
					public void updateItem( String item, boolean empty )
					{
						super.updateItem( item, empty );
						if ( empty )
						{
							setGraphic( null );
							setText( null );
						}
						else
						{
							btn.setOnAction( ( ActionEvent event ) ->
							{
								DatabaseStructure dbStruct = getTableView().getItems().get( getIndex() );
								switch(dbStruct.typeProperty().get()){
									case "View" :
										database.view(dbStruct.nameProperty().get());
										break;
									case "Table" :
										database.table(dbStruct.nameProperty().get()).get();
										break;
								}
							} );
							setGraphic( btn );
							setText( null );
						}
					}
				};
				return cell;
			}
		});
		colDelete.setCellFactory(new Callback<TableColumn<DatabaseStructure, String>, TableCell<DatabaseStructure, String>>() {
			@Override
			public TableCell call( final TableColumn<DatabaseStructure, String> param )
			{
				final TableCell<DatabaseStructure, String> cell = new TableCell<DatabaseStructure, String>()
				{
					final Button btn = new Button( "Drop" );

					@Override
					public void updateItem( String item, boolean empty )
					{
						super.updateItem( item, empty );
						if ( empty )
						{
							setGraphic( null );
							setText( null );
						}
						else
						{
							btn.setOnAction( ( ActionEvent event ) ->
							{
								DatabaseStructure dbStruct = getTableView().getItems().get( getIndex() );
								switch(dbStruct.typeProperty().get()){
									case "View" :
										database.view(dbStruct.nameProperty().get()).get().drop();
										break;
									case "Table" :
										database.table(dbStruct.nameProperty().get()).get().drop();
										break;
								}
							} );
							setGraphic( btn );
							setText( null );
						}
					}
				};
				return cell;
			}
		});

		tabPane.getSelectionModel().clearSelection();
	}

	/**
	 * Display the structure of the database
	 */
	private void displayStructure() {
		dbStructs.clear();
		dbStructs.addAll(Stream.concat(database.tables(), database.views())
		                       .sorted((a, b) -> a.name().compareTo(b.name()))
		                       .map(DatabaseStructure::new)
		                       .collect(Collectors.toList()));


		structureTable.setItems(dbStructs);
	}

	/**
	 * Set the database
	 *
	 * @param database
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}
}
