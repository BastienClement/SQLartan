package sqlartan.view.util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;
import java.util.Optional;

/**
 * Created by Adriano on 04.05.2016.
 */
public class Popup {

	/**
	 * A error popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static void error(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(null);
		alert.showAndWait();
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
		Alert alert = new Alert(Alert.AlertType.WARNING);
		alert.setHeaderText(null);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.getButtonTypes().setAll(buttonTypes);
		return alert.showAndWait();

	}

	/**
	 * A information popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static void information(String title, String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
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