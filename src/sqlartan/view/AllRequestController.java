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
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import sqlartan.core.Database;
import sqlartan.core.Result;
import java.sql.SQLException;

public class AllRequestController {

	private DataTableView dataTableView = new DataTableView();

	@FXML
	Button execute;

	@FXML
	TextArea SQLTextQuery;

	@FXML
	StackPane userQueryView;

	private Database db = SqlartanController.getDB();

	@FXML
	private void initialize() {
		userQueryView.getChildren().add(new TableView<>());
	}

	/**
	 * Execute the query
	 */
	public void executeQuery() {
		userQueryView.getChildren().clear();
		try {
			Result result = db.execute(SQLTextQuery.getText());
			userQueryView.getChildren().add(result.isQueryResult() ? dataTableView.getTableView(result)
					: new Text(Long.toString(result.updateCount()) + " rows updated"));
		} catch (SQLException e) {
			userQueryView.getChildren().add(new Text(e.getMessage()));
		}
	}


}
