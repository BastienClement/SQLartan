package sqlartan.view.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import sqlartan.core.Table;
import sqlartan.view.SqlartanController;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Created by Adriano on 04.05.2016.
 */
public class Popup {

	/**
	 *  An error popup
	 * @param message
	 */
	public static void error(String title, String message){
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(null);
		alert.showAndWait();
	}

	/**
	 * A warning popup
	 * @param title the title
	 * @param message the message
	 * @param buttonTypes The button types
	 * @return One of the button types
	 */
	public static Optional<ButtonType> warning(String title, String message, ButtonType... buttonTypes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setHeaderText(null);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.getButtonTypes().setAll(buttonTypes);
		return alert.showAndWait();

	}

	public static void information(String title, String message){
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public static Optional<String> input(String title, String message, String placeholder){
		TextInputDialog dialog = new TextInputDialog(placeholder);
		dialog.setTitle(title);
		dialog.setContentText(message);
		dialog.setHeaderText(null);
		return dialog.showAndWait();

	}
}
