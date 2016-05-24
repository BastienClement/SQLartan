package sqlartan.view;

import sqlartan.core.ResultColumn;
import sqlartan.core.Row;

/**
 * Projet : SQLartan
 * Créé le 24.05.2016.
 *
 * @author Adriano Ruberto
 */
public class EditBidon {
	public final Row row;
	public final ResultColumn column;
	public String string;

	public EditBidon(Row row, ResultColumn column, String string) {
		this.row = row;
		this.column = column;
		this.string = string;
	}

	public EditBidon update(String string) {
		return new EditBidon(row, column, string);
	}
}
