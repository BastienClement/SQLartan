package sqlartan.gui.util;

import sqlartan.core.ResultColumn;
import sqlartan.core.Row;

/**
 * Represent the model for the EditCell.
 *
 */
public class EditModel {
	public final Row row;
	public final ResultColumn column;
	public final String text;

	public EditModel(Row row, ResultColumn column, String text) {
		this.row = row;
		this.column = column;
		this.text = text;
	}

	public EditModel update(String text) {
		return new EditModel(row, column, text);
	}
}
