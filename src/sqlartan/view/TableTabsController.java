package sqlartan.view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.core.PersistentStructure;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by julien on 29.04.16.
 */
public class TableTabsController {

	private PersistentStructure<?> structure;

	private TableVue tableVue = new TableVue();

	@FXML
	private TabPane tabPane;

	@FXML
	private Tab displayTab;

	@FXML
	private Pane sqlTab;

	@FXML
	private TableView structureTab;

	@FXML
	private void initialize()
	{
		FXMLLoader allRequestLoader = new FXMLLoader(Sqlartan.class.getResource("view/AllRequest.fxml"));

		try {
			Pane allRequestPane = allRequestLoader.load();

			sqlTab.getChildren().add(allRequestPane);

			allRequestPane.prefHeightProperty().bind(sqlTab.heightProperty());
			allRequestPane.prefWidthProperty().bind(sqlTab.widthProperty());


		} catch (IOException e) {
			e.printStackTrace();
		}

		// Affiche la tablea dans l'onglet display l'orque il est acctive
		tabPane.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Tab>() {

			@Override
			public void changed(ObservableValue<? extends Tab> observable, Tab oldTab, Tab newTab) {
				if(newTab == displayTab) { displayTab.setContent(tableVue.getTableView(structure)); }

			}

		});

	}

	@FXML
	private void testPint()
	{
		System.out.println("Fonction de test");
	}

	public void setStructure(PersistentStructure<?> structure)
	{
		this.structure = structure;
	}

}
