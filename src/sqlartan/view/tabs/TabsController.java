package sqlartan.view.tabs;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.view.tabs.model.Model;
import java.io.IOException;

/**
 * Projet : SQLartan
 * Créé le 24.05.16.
 *
 * @author Julien Leroy
 */
public abstract class TabsController {

	@FXML
	protected TableColumn<Model, String> colName;
	@FXML
	protected TableColumn<Model, String> colType;

	@FXML
	protected TabPane tabPane;
	@FXML
	protected Tab structureTab;
	@FXML
	protected Pane sqlPane;
	@FXML
	protected Tab sqlTab;
	@FXML
	private SqlTabController sqlTabController;


	/**
	 * Initialize tabs structure and sql
	 *
	 * @throws IOException
	 */
	@FXML
	protected void initialize() throws IOException {
		FXMLLoader loader = new FXMLLoader(Sqlartan.class.getResource("view/tabs/sqlTab.fxml"));

		tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
			refresh(newTab);
		});

		Pane pane = loader.load();

		sqlPane.getChildren().add(pane);
		sqlTabController = loader.getController();
		pane.prefHeightProperty().bind(sqlPane.heightProperty());
		pane.prefWidthProperty().bind(sqlPane.widthProperty());

		sqlTab = sqlTabController;

		colName.setCellValueFactory(param -> param.getValue().name);
		colType.setCellValueFactory(param -> param.getValue().type);

		tabPane.getSelectionModel().clearSelection();
	}


	/**
	 * Call when the tab structure is clicked
	 */
	protected abstract void displayStructure();


	/**
	 * Call when a tab is selected, refresh the controller
	 *
	 * @param selected the selected tab
	 */
	protected abstract void refresh(Tab selected);


	/**
	 * Refresh the selected tab
	 */
	public void refresh() {
		refresh(tabPane.getSelectionModel().getSelectedItem());
	}


	/**
	 * Set the text in the sqlTab
	 *
	 * @param request the request to put
	 */
	public void setSqlRequest(String request) {
		sqlTabController.setRequest(request);
	}


	/**
	 * Select the sql tab
	 */
	public void selectSqlTab() {
		tabPane.getSelectionModel().selectFirst();
		tabPane.getSelectionModel().selectLast();
	}
}
