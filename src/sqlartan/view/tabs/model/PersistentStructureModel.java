package sqlartan.view.tabs.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.Column;

/**
 *
 */
public class PersistentStructureModel extends StructureModel {
	public final IntegerProperty no;
	public final StringProperty nullable;
	public final StringProperty defaultValue;
	public final StringProperty comment;

	public PersistentStructureModel(Column column, int ID) {
		super(column.name(), column.type());
		this.no = new SimpleIntegerProperty(ID);
		this.nullable = new SimpleStringProperty(column.nullable() ? "True" : "False");
		this.defaultValue = new SimpleStringProperty(); // TODO
		this.comment = new SimpleStringProperty(); // TODO
	}
}
