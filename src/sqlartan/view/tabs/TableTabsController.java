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
import sqlartan.core.PersistentStructure;
import sqlartan.core.Table;
import sqlartan.view.DataTableView;
import sqlartan.view.SqlartanController;
import sqlartan.view.tabs.struct.DatabaseStructure;
import sqlartan.view.tabs.struct.TableStructure;
import sqlartan.view.util.Popup;


/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController extends TabsController {

	@FXML
	private TableColumn<TableStructure, String> colNo;
	@FXML
	private TableColumn<TableStructure, String> colName;
	@FXML
	private TableColumn<TableStructure, String> colType;
	@FXML
	private TableColumn<TableStructure, String> colNull;
	@FXML
	private TableColumn<TableStructure, String> colDefaultValue;
	@FXML
	private TableColumn<TableStructure, String> colComment;
	@FXML
	private TableColumn<TableStructure, String> colRename;
	@FXML
	private TableColumn<TableStructure, String> colDelete;

	private PersistentStructure<?> structure;

	private DataTableView dataTableView = new DataTableView();

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab displayTab;

	@FXML
	private Tab structureTab;

	@FXML
	private Pane sqlTab;

	@FXML
	private TableView<TableStructure> structureTable;

	private Database database;

	private SqlartanController controller;

	private Table table;

	@FXML
	private void initialize() {


		addPane(new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml")), sqlTab);

		/**
		 * Display the datas from the tableStructures in display tab only when he's active.
		 * Every time a new query is done.
		 */
		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			if (newTab == displayTab) {
				displayTab.setContent(dataTableView.getTableView(structure));
			} else if (newTab == structureTab) {
				displayStructure();
			}
		});

		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());
		colRename.setCellFactory(new Callback<TableColumn<TableStructure, String>, TableCell<TableStructure, String>>() {
			@Override
			public TableCell call( final TableColumn<TableStructure, String> param )
			{
				final TableCell<TableStructure, String> cell = new TableCell<TableStructure, String>()
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
								TableStructure tableStruct = getTableView().getItems().get( getIndex() );
								Popup.input("Rename", "Rename " + tableStruct.nameProperty().get() + " into : ",  tableStruct.nameProperty().get()).ifPresent(name -> {
									if (name.length() > 0 && ! tableStruct.nameProperty().get().equals(name)) {
										controller.renameColumn(table, tableStruct.nameProperty().get(), name);
									}
								});
							} );
							setGraphic( btn );
							setText( null );
						}
					}
				};
				return cell;
			}
		});
		colDelete.setCellFactory(new Callback<TableColumn<TableStructure, String>, TableCell<TableStructure, String>>() {
			@Override
			public TableCell call( final TableColumn<TableStructure, String> param )
			{
				final TableCell<TableStructure, String> cell = new TableCell<TableStructure, String>()
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
								TableStructure tableStruct = getTableView().getItems().get( getIndex() );
								controller.dropColumn(table, tableStruct.nameProperty().get());
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

	public void setStructure(PersistentStructure<?> structure) {
		this.structure = structure;
	}

	private void displayStructure() {
		ObservableList<TableStructure> tableStructures = FXCollections.observableArrayList();

		TableStructure.IDReset();
		tableStructures.addAll(structure.columns()
		                                .map(TableStructure::new)
		                                .toList());


			structureTable.setItems(tableStructures);
	}

	/**
	 * Set the database
	 *
	 * @param database
	 */
	public void setDatabase(Database database) {
		this.database = database;
	}

	/**
	 * Set the controller
	 *
	 * @param controller
	 */
	public void setController(SqlartanController controller) {
		this.controller = controller;
	}

	/**
	 * Set the table
	 *
	 * @param table
	 */
	public void setTable(Table table) {
		this.table = table;
	}
}
