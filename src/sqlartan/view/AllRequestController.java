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
import sqlartan.core.Database;
import sqlartan.core.Result;
import java.sql.SQLException;

public class AllRequestController {
	@FXML
	Button execute;

	@FXML
	TextArea SQLTextQuery;

	@FXML
	TableView UserQueryView;

	Database db;

	public void executeQuery() {
		try {
			dataView(db.assemble(SQLTextQuery.toString()).execute(), UserQueryView);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void dataView(Result execute, TableView tv) {
		// TODO Use the SqlartanController.dataView()
	}

}
