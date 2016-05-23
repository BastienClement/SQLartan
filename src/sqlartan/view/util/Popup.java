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

	/**
	 * A inputDialog popup
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param message2    the second message
	 * @return An optional of the inputed string
	 */
	public static Optional<Pair<String, String>> doubleInput(String title, String message, String message2) {
		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle(title);
		dialog.setHeaderText(null);

		// Set the button types.
		ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

		// Create the two labels and fields
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField messageField = new TextField();
		messageField.setPromptText(message);
		TextField message2Field = new TextField();
		message2Field.setPromptText(message2);

		grid.add(new Label(message), 0, 0);
		grid.add(messageField, 1, 0);
		grid.add(new Label(message2), 0, 1);
		grid.add(message2Field, 1, 1);

		dialog.getDialogPane().setContent(grid);

		// Convert the result to a username-password-pair when the login button is clicked.
		dialog.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				return new Pair<>(messageField.getText(), message2Field.getText());
			}
			return null;
		});

		return dialog.showAndWait();
	}
}