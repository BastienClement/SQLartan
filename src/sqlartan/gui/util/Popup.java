package sqlartan.gui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.util.Optional;

/**
 * A popup utility class
 */
public class Popup {

	/**
	 * Create an alert with a specific type.
	 *
	 * @param title   the title
	 * @param message the message
	 * @param header  the header
	 * @param type    the type
	 * @return the alert from the type
	 */
	private static Alert alert(String title, String message, String header, Alert.AlertType type) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setContentText(message);
		alert.setHeaderText(header);
		return alert;
	}


	/**
	 * A error popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static Optional<ButtonType> error(String title, String message) {
		return alert(title, message, null, Alert.AlertType.ERROR).showAndWait();
	}


	/**
	 * A warning popup with header
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param header      the header
	 * @param buttonTypes The button types
	 * @return An optional of one of the button types
	 */
	public static Optional<ButtonType> warning(String title, String message, String header, ButtonType... buttonTypes) {
		Alert alert = alert(title, message, header, Alert.AlertType.WARNING);
		alert.getButtonTypes().setAll(buttonTypes);
		return alert.showAndWait();
	}


	/**
	 * A warning popup without header
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param buttonTypes The button types
	 * @return An optional of one of the button types
	 */
	public static Optional<ButtonType> warning(String title, String message, ButtonType... buttonTypes) {
		return warning(title, message, null, buttonTypes);
	}


	/**
	 * A information popup
	 *
	 * @param title   the title
	 * @param message the message
	 */
	public static Optional<ButtonType> information(String title, String message) {
		return alert(title, message, null, Alert.AlertType.INFORMATION).showAndWait();
	}


	/**
	 * create an TextInputDialog and show it
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param placeholder the placeholder
	 * @return An optional of the inputted text
	 */
	public static Optional<String> input(String title, String message, String placeholder) {
		return input(title, message, placeholder, null);
	}


	/**
	 * create an TextInputDialog with an header and show it
	 *
	 * @param title       the title
	 * @param message     the message
	 * @param placeholder the placeholder
	 * @param header      the header
	 * @return An optional of the inputted text
	 */
	public static Optional<String> input(String title, String message, String placeholder, String header) {
		TextInputDialog dialog = new TextInputDialog(placeholder);
		dialog.setTitle(title);
		dialog.setContentText(message);
		dialog.setHeaderText(header);
		return dialog.showAndWait();
	}


	/**
	 * Create a FileChooser
	 *
	 * @param title  the title of the window
	 * @param filter the filter to be applied on the file chooser
	 * @return The created file chooser
	 */
	private static FileChooser file(String title, FileChooser.ExtensionFilter filter) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle(title);
		fileChooser.setSelectedExtensionFilter(filter);
		return fileChooser;
	}


	/**
	 * Show a save dialog file chooser.
	 *
	 * @param title  the title of the window
	 * @param window the owner window of the displayed file dialog
	 * @param filter the filter to be applied on the file chooser
	 * @return An optional of the file chosen by the user
	 */
	public static Optional<File> save(String title, Window window, FileChooser.ExtensionFilter filter) {
		return Optional.ofNullable(file(title, filter).showSaveDialog(window));
	}


	/**
	 * Show a browse dialog file chooser.
	 *
	 * @param title  the title of the window
	 * @param window the owner window of the displayed file dialog
	 * @param filter the filter to be applied on the file chooser
	 * @return An optional of the file chosen by the user
	 */
	public static Optional<File> browse(String title, Window window, FileChooser.ExtensionFilter filter) {
		return Optional.ofNullable(file(title, filter).showOpenDialog(window));
	}

}