package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.PersistentStructure;
import sqlartan.view.DataTableView;
import sqlartan.view.tabs.struct.TableStructure;


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
	private TableColumn<TableStructure, String> colActions;

	@FXML
	private TableColumn<InsertRowStructure, String> insertColName;
	@FXML
	private  TableColumn<InsertRowStructure, String> insertColType;
	@FXML
	private TableColumn<InsertRowStructure, String> insertColValue;


	private PersistentStructure<?> structure;

	private DataTableView dataTableView = new DataTableView();

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab displayTab;

	@FXML
	private Tab structureTab;

	@FXML
	private Tab insertTab;

	@FXML
	private Pane sqlTab;

	@FXML
	private TableView<InsertRowStructure> insertTable;

	@FXML
	private TableView<TableStructure> structureTable;


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
			} else if (newTab == insertTab){
				displayInsertTab();
			}
		});

		colActions.setCellValueFactory(param -> param.getValue().actionProperty());
		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		insertTable.setEditable(true);

		insertColName.setCellValueFactory(param -> param.getValue().name);
		insertColType.setCellValueFactory(param -> param.getValue().type);
		insertColValue.setCellValueFactory(param -> param.getValue().value);

		insertColValue.setCellFactory(TextFieldTableCell.forTableColumn()); //TODO eventuelement modifier pour toujour aficher le textfield
		insertColValue.setOnEditCommit(event -> {
			event.getTableView().getItems().get(event.getTablePosition().getRow()).setValue(event.getNewValue());
		});

		insertColValue.setEditable(true);


		tabPane.getSelectionModel().clearSelection();

	}

	private void displayInsertTab(){
		ObservableList<InsertRowStructure> insertRows = FXCollections.observableArrayList();

		insertRows.addAll(structure.columns().map(InsertRowStructure::new).toList());


		insertTable.setItems(insertRows);


	}

	@FXML
	private void submitNewData()
	{
		ObservableList<InsertRowStructure> insertRows = insertTable.getItems();
		//TODO call the insert methode on the core
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

}
