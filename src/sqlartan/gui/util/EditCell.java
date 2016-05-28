package sqlartan.gui.util;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import sqlartan.core.Affinity;
import sqlartan.core.util.UncheckedSQLException;

/**
 * Represent a editable cell. When the user double click on a cell, a TextField is created and let the user edit the
 * cell
 */
public class EditCell extends TableCell<ObservableList<EditModel>, EditModel> {
	private TextField textField;

	/** {@inheritDoc} */
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


	/** {@inheritDoc} */
	@Override
	public void cancelEdit() {
		super.cancelEdit();
		setText(getItem().text);
		setGraphic(null);
	}


	/** {@inheritDoc} */
	@Override
	public void updateItem(EditModel item, boolean empty) {
		super.updateItem(item, empty);
		setText(null);
		setGraphic(null);
		if (isEditing()) {
			if (textField != null) {
				textField.setText(text());
			}
			setGraphic(textField);
		} else {
			setText(text());
		}

	}


	/**
	 * Create a text field. When the user press enter, the text will be committed and will update the row.
	 */
	private void createTextField() {
		textField = new TextField(text());
		textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
		textField.setOnKeyReleased(t -> {
			switch (t.getCode()) {
				case ENTER:
					try {
						getItem().row.update(getItem().column, Affinity.forType(getItem().column.type()).type.convert(textField.getText()));
						commitEdit(getItem().update(textField.getText()));
					} catch (java.lang.IllegalArgumentException | UncheckedSQLException e) {
						cancelEdit();
						throw e;
					}
					break;
				case ESCAPE:
					cancelEdit();
					break;
			}
		});
	}


	/**
	 * @return "" if null, otherwise return the text of the item
	 */
	private String text() {
		return getItem() == null ? "" : getItem().text;
	}

}
