package sqlartan.gui.util;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import java.util.function.BiConsumer;

/**
 * Projet : SQLartan
 * Créé le 26.05.2016.
 *
 * @author Adriano Ruberto
 */
public class ActionButtons {

	public static <T> Callback<TableColumn<T, String>, TableCell<T, String>>
	actionButton(String label, BiConsumer<TableCell<T, String>, ActionEvent> action) {
		return param -> new TableCell<T, String>() {
			private Button btn = new Button(label);
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
