package sqlartan.gui.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import sqlartan.gui.controller.SqlartanController;
import sqlartan.gui.util.Popup;
import java.io.File;

/**
 * Created by julien on 02.05.16.
 */
public class AttachedChooserController {

	@FXML
	private Button ok;

	@FXML
	private Button cancel;

	@FXML
	private Button browse;

	@FXML
	private TextField path;

	@FXML
	private Pane attachedPane;

	@FXML
	private TextField dbName;


	private SqlartanController controller;

	File file = null;


	@FXML
	private void initialize()
	{
	}

	@FXML
	private void close()
	{
		((Stage)attachedPane.getScene().getWindow()).close();
	}

	public void setController(SqlartanController controller)
	{
		this.controller = controller;
	}

	@FXML
	private void validate()
	{
		file = new File(path.getText());

		if (!file.getPath().isEmpty() && !dbName.getText().isEmpty()) {
			controller.attachDatabase(file, dbName.getText());
			close();
		}
		else
		{
			Popup.error("Invalid Entry","The informations for the path and/or the database name are empty");
		}

	}

	@FXML
	private void browse()
	{
		String oldPath = path.getText();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Selecte database to attached");
		file = fileChooser.showOpenDialog(attachedPane.getScene().getWindow());

		if (file != null) {
			if (dbName.getText().isEmpty() || (!oldPath.isEmpty() && fileName(new File(oldPath)).equals(dbName.getText())))
			{
				dbName.setText(fileName(file));
			}

			path.setText(file.getPath());
		}
	}

	private String fileName(File file)
	{
		return file.getName().split("\\.")[0];
	}
}
