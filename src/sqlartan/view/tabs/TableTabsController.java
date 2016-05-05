package sqlartan.view.tabs;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

		colActions.setCellValueFactory(param -> param.getValue().actionProperty());
		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

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

}
