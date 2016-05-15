package sqlartan.view;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

// EditCell - for editing capability in a TableCell
public class EditCell extends TableCell {
	private TextField textField;

	@Override
	public void startEdit() {
		if (!isEmpty()) {
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

		try {
			setText(getItem().toString());
		} catch (Exception e) {
		}
		setGraphic(null);
	}

	@Override
	public void updateItem(Object item, boolean empty) {
		System.out.println("find value of update: " + empty + " " + item);
		super.updateItem(item, empty);
		if (empty) {
			setText(null);
			setGraphic(null);
		} else {
			if (isEditing()) {
				if (textField != null) {
					textField.setText(getString());
				}
				setText(null);
				setGraphic(textField);
			} else {
				setText(getString());
				setGraphic(null);
			}
		}
	}

	private void createTextField() {
		textField = new TextField(getString());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.setOnKeyReleased(t -> {
			if (t.getCode() == KeyCode.ENTER) {
				commitEdit(textField.getText());
			} else if (t.getCode() == KeyCode.ESCAPE) {
				cancelEdit();
			}
		});
	}

	private String getString() {
		return getItem() == null ? "" : getItem().toString();
	}

}