package sqlartan.view;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

/**
 * Created the 15 May 2016
 *
 * @author Adriano Ruberto
 * @version 1.0
 */
public class EditCell extends TableCell<ObservableList<EditBidon>, EditBidon> {
	private TextField textField;

	@Override
	public void startEdit() {
		if (!isEmpty() && getItem().row.editable()) {
			super.startEdit();
			createTextField();
			setText(null);
			setGraphic(textField);
			textField.selectAll();
			Platform.runLater(() -> textField.requestFocus());
		}
	}

	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getItem().string);
		setGraphic(null);
	}

	@Override
	public void updateItem(EditBidon item, boolean empty) {
		super.updateItem(item, empty);
		setText(null);
		setGraphic(null);
		if (isEditing()) {
			if (textField != null) {
				textField.setText(getString());
			}
			setGraphic(textField);
		} else {
			setText(getString());
		}

	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.setOnKeyReleased(t -> {
			if (t.getCode() == KeyCode.ENTER) {
				commitEdit(getItem().update(textField.getText()));
			} else if (t.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});
	}

	private String getString() {
		return getItem() == null ? "" : getItem().string;
	}

}
