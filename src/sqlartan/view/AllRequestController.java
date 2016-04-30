package sqlartan.view;
/*
 * Projet : SQLartan
 * Créé le 29.04.2016.
 * Auteur : Adriano Ruberto
 */

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import sqlartan.core.Database;
import sqlartan.core.Result;
import java.sql.SQLException;

public class AllRequestController {

	private TableVue tableVue = new TableVue();

	@FXML
	Button execute;

	@FXML
	TextArea SQLTextQuery;

	@FXML
	StackPane userQueryView;

	private Database db;

	@FXML
	private void initialize()
	{
		userQueryView.getChildren().add(new TableView<>());
	}

	public void executeQuery() {
		userQueryView.getChildren().clear();
		userQueryView.getChildren().add(tableVue.getTableView(SQLTextQuery.getText()));
	}


}
