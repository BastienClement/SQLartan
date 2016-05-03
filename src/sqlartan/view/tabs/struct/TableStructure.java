package sqlartan.view.tabs.struct;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.Column;

/**
 * Projet : SQLartan
 * Créé le 03.05.2016.
 *
 * @author Adriano Ruberto
 */
public class TableStructure {
	private static int ID = 0;
	private StringProperty no;
	private StringProperty name;
	private StringProperty type;
	private StringProperty nullable;
	private StringProperty defaultValue;
	private StringProperty comment;
	private StringProperty action;

	public TableStructure(Column column) {
		this.no = new SimpleStringProperty(Integer.toString(++ID));
		this.name = new SimpleStringProperty(column.name());
		this.type = new SimpleStringProperty(column.type());
		this.nullable = new SimpleStringProperty(column.nullable() ? "True" : "False");
		this.defaultValue = new SimpleStringProperty(); // TODO
		this.comment = new SimpleStringProperty(); // TODO
		this.action = new SimpleStringProperty(); // TODO

	}

	public static void IDReset() {ID = 0;}

	public StringProperty noProperty() {
		return no;
	}
	public StringProperty nameProperty() {
		return name;
	}
	public StringProperty actionProperty() {
		return action;
	}
	public StringProperty typeProperty() {
		return type;
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
