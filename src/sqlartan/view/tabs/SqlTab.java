package sqlartan.view.tabs;
/*
 * Projet : SQLartan
 * Créé le 29.04.2016.
 * Auteur : Adriano Ruberto
 */

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.core.Result;
import sqlartan.view.DataTableView;
import java.sql.SQLException;

public class SqlTab extends Tab {

	@FXML
	Button execute;
	@FXML
	TextArea SQLTextQuery;
	@FXML
	StackPane userQueryView;

	@FXML
	private void initialize() {
		userQueryView.getChildren().add(new TableView<>());
	}

	/**
	 * Execute the query
	 */
	public void executeQuery() {
		Database db = Sqlartan.getInstance().getController().database();
		userQueryView.getChildren().clear();
		try {
			Result result = db.execute(SQLTextQuery.getText());
			if (result.isQueryResult()) {
				userQueryView.getChildren().add(DataTableView.getTableView(result));
			} else {
				userQueryView.getChildren().add(new Text(Long.toString(result.updateCount()) + " row(s) updated"));
				Sqlartan.getInstance().getController().refreshView();
			}
		} catch (SQLException e) {
			userQueryView.getChildren().add(new Text(e.getMessage()));
		}
	}

	public void setRequest(String request) {
		SQLTextQuery.setText(request);
	}


}
