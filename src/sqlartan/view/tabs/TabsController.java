package sqlartan.view.tabs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.view.DataTableView;
import sqlartan.view.tabs.structureTab.TableStructureTab;
import java.io.IOException;

/**
 * Created by julien on 24.05.16.
 */
public abstract class TabsController {

	@FXML
	protected TableColumn<TableStructureTab, Number> colNo;
	@FXML
	protected TableColumn<TableStructureTab, String> colName;
	@FXML
	protected TableColumn<TableStructureTab, String> colType;
	@FXML
	protected TableColumn<TableStructureTab, String> colNull;
	@FXML
	protected TableColumn<TableStructureTab, String> colDefaultValue;
	@FXML
	protected TableColumn<TableStructureTab, String> colComment;

	protected DataTableView dataTableView = new DataTableView();

	@FXML
	protected TabPane tabPane;

	@FXML
	protected Tab structureTab;

	@FXML
	protected Pane sqlPane;

	@FXML
	protected TableView<TableStructureTab> structureTable;


	@FXML
	protected void initialize() throws IOException {
		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		Pane pane = loader.load();
		sqlPane.getChildren().add(pane);

		pane.prefHeightProperty().bind(sqlPane.heightProperty());
		pane.prefWidthProperty().bind(sqlPane.widthProperty());

		/**
		 * Display the datas from the tableStructures in display tab only when he's active.
		 * Every time a new query is done.
		 */


		colComment.setCellValueFactory(param -> param.getValue().commentProperty());
		colDefaultValue.setCellValueFactory(param -> param.getValue().defaultValueProperty());
		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colNo.setCellValueFactory(param -> param.getValue().noProperty());
		colNull.setCellValueFactory(param -> param.getValue().nullableProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		tabPane.getSelectionModel().clearSelection();

	}


	/**
	 * Display the structure, if the structure can't be displayed, a popup will ask the user if he want to drop it.
	 */
	protected abstract void displayStructure();

}
