package sqlartan.view.attached;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sqlartan.Sqlartan;
import sqlartan.core.Database;
import sqlartan.view.SqlartanController;
import java.io.File;

/**
 * Created by julien on 02.05.16.
 */
public class AttachedChooserController {

	public Button ok;
	public Button cancel;
	public Button browse;
	public TextField path;
	public Pane attachedPane;
	public TextField dbName;

	private SqlartanController sqlartanController;

	File file = null;


	@FXML
	private void close()
	{
		((Stage)attachedPane.getScene().getWindow()).close();
	}

	public void setSqlartanController(SqlartanController controller)
	{
		sqlartanController = controller;
	}

	@FXML
	private void validate()
	{
		if (file != null) {
			sqlartanController.attachDatabase(file, dbName.getText());
		}

		close();
	}

	@FXML
	private void browse()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Selecte database to attached");
		file = fileChooser.showOpenDialog(attachedPane.getScene().getWindow());

		path.setText(file.getPath());
		dbName.setText(file.getName().split("\\.")[0]);
	}
}
