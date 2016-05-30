package sqlartan.gui.util;

import sqlartan.core.ResultColumn;
import sqlartan.core.Row;
import sqlartan.core.Type;

/**
 * The model for the EditCell.
 */
public class EditModel implements Comparable<EditModel> {
	public final Row row;
	public final ResultColumn column;
	public final String text;

	public EditModel(Row row, ResultColumn column, String text) {
		this.row = row;
		this.column = column;
		this.text = text;
	}

	/**
	 * Returns an updated EditModel with a new text
	 *
	 * @param text the new text of the EditModel
	 * @return the updated EditModel
	 */
	public EditModel update(String text) {
		return new EditModel(row, column, text);
	}

	/**
	 * Implements compareTo to sort correctly each columns type.
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(EditModel o) {
		Type mine = column.affinity().type;
		Type other = o.column.affinity().type;
		if (mine == other) {
			try {
				switch (mine) {
					case Null:
						return 0;
					case Integer:
						return Integer.parseInt(text) - Integer.parseInt(o.text);
					case Real:
						return (int) (Double.parseDouble(text) - Double.parseDouble(o.text));
					case Text:
						return text.compareTo(o.text);
					case Blob:
						return 0;
				}
			} catch (Exception e) { // If the format doesn't work, we use the textual one.
				return text.compareTo(o.text);
			}
		}

		return mine.ordinal() - other.ordinal();
	}
}
