package sqlartan.view.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

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
	public static ButtonType warning(String title, String message, ButtonType... buttonTypes) {
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.getButtonTypes().setAll(buttonTypes);
		return alert.showAndWait().get();

	}

	public static void information(String title, String message){
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
