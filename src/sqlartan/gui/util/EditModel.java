package sqlartan.gui.util;

import sqlartan.core.ResultColumn;
import sqlartan.core.Row;

/**
 * Projet : SQLartan
 * Créé le 24.05.2016.
 *
 * @author Adriano Ruberto
 */
public class EditModel {
	public final Row row;
	public final ResultColumn column;
	public String string;

	public EditModel(Row row, ResultColumn column, String string) {
		this.row = row;
		this.column = column;
		this.string = string;
	}

	public EditModel update(String string) {
		return new EditModel(row, column, string);
	}
}
