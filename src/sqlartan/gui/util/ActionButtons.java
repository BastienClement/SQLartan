package sqlartan.gui.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.util.function.BiConsumer;

/**
 * This class is used for create dynamic button
 */
public class ActionButtons {

	/**
	 * Create an action button
	 *
	 * @param label  the label of the button
	 * @param action the action when the button is pressed
	 * @param <T>    the type of the TableView/TableCell generic type
	 * @return the created button
	 */
	public static <T> Callback<TableColumn<T, String>, TableCell<T, String>>
	actionButton(String label, BiConsumer<TableCell<T, String>, ActionEvent> action) {
		return param -> new TableCell<T, String>() {
			private final Button btn = new Button(label);
			public void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
					setText(null);
				} else {
					btn.setOnAction(event -> action.accept(this, event));
					setGraphic(btn);
					setText(null);
				}
			}
		};
	}
}
