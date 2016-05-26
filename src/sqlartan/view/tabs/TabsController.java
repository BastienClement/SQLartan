package sqlartan.view.tabs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.view.SqlTab;
import sqlartan.view.DataTableView;
import sqlartan.view.tabs.structureTab.StructureTab;
import sqlartan.view.tabs.structureTab.TableStructureTab;
import java.io.IOException;

/**
 * Created by julien on 24.05.16.
 */
public abstract class TabsController<T extends StructureTab> {

	@FXML
	protected TableColumn<T, String> colName;
	@FXML
	protected TableColumn<T, String> colType;

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
	protected Tab sqlTab;
	@FXML
	private SqlTab allRequestControler;

	@FXML
	protected void initialize() throws IOException {
		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		Pane pane = loader.load();

		sqlPane.getChildren().add(pane);
		allRequestControler = loader.getController();
		pane.prefHeightProperty().bind(sqlPane.heightProperty());
		pane.prefWidthProperty().bind(sqlPane.widthProperty());

		sqlTab = allRequestControler;

		colName.setCellValueFactory(param -> param.getValue().nameProperty());
		colType.setCellValueFactory(param -> param.getValue().typeProperty());

		tabPane.getSelectionModel().clearSelection();
	}


	/**
	 * Display the structure, if the structure can't be displayed, a popup will ask the user if he want to drop it.
	 */
	protected abstract void displayStructure();


	/**
	 * TODO
	 *
	 * @param request
	 */
	public void setSqlRequest(String request) {
		allRequestControler.setRequest(request);
	}


	/**
	 * TODO
	 */
	public void selectSqlTab() {
		tabPane.getSelectionModel().select(sqlTab);
	}

}
