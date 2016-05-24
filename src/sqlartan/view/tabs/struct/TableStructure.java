package sqlartan.view.tabs.struct;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.Column;

/**
 * Projet : SQLartan
 * Créé le 03.05.2016.
 *
 * @author Adriano Ruberto
 *
 * Represent the structure tab for a view or a table
 */
public class TableStructure extends TabStructure {
	private static int ID = 0;
	private IntegerProperty no;
	private StringProperty nullable;
	private StringProperty defaultValue;
	private StringProperty comment;

	public TableStructure(Column column) {
		super(column.name(), column.type());
		this.no = new SimpleIntegerProperty(++ID);
		this.nullable = new SimpleStringProperty(column.nullable() ? "True" : "False");
		this.defaultValue = new SimpleStringProperty(); // TODO
		this.comment = new SimpleStringProperty(); // TODO

	}

	public static void IDReset() {ID = 0;}

	public IntegerProperty noProperty() {
		return no;
	}
	public StringProperty nullableProperty() {
		return nullable;
	}
	public StringProperty defaultValueProperty() {
		return defaultValue;
	}
	public StringProperty commentProperty() {
		return comment;
	}
}
