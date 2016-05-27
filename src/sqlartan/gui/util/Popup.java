package sqlartan.gui.util;

import javafx.scene.control.*;
import java.util.Optional;

/**
 * Created by Adriano on 04.05.2016.
 */
public class Popup {

	/**
	 * A private function to factore the public alert.
	 *
	 * @param title   the title
	 * @param message the message
	 * @param type    the type
	 * @return the alert from the type
	 */
	private static Alert alert(String title, String message, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(null);
		return alert;
	}
	/**
	 * A error popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static Optional<ButtonType> error(String title, String message) {
		return alert(title, message, Alert.AlertType.ERROR).showAndWait();
	}

	/**
	 * A warning popup
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param buttonTypes The button types
	 * @return An optional of one of the button types
	 */
	public static Optional<ButtonType> warning(String title, String message, ButtonType... buttonTypes) {
		Alert alert = alert(title, message, Alert.AlertType.WARNING);
		alert.getButtonTypes().setAll(buttonTypes);
		return alert.showAndWait();

	}

	/**
	 * A information popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static Optional<ButtonType> information(String title, String message) {
		return alert(title, message, Alert.AlertType.INFORMATION).showAndWait();
	}

	/**
	 * A inputDialog popup
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param placeholder the placeholder
	 * @return An optional of the inputed string
	 */
	public static Optional<String> input(String title, String message, String placeholder) {
		TextInputDialog dialog = new TextInputDialog(placeholder);
		dialog.setTitle(title);
		dialog.setContentText(message);
		dialog.setHeaderText(null);
		return dialog.showAndWait();
	}
}