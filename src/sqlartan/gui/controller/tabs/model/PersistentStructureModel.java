package sqlartan.gui.controller.tabs.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.Column;

/**
 * The model of the structure tab of a PersistentStructure.
 */
public class PersistentStructureModel extends StructureModel {
	public final IntegerProperty no;
	public final StringProperty nullable;

	public PersistentStructureModel(Column column, int ID) {
		super(column.name(), column.type());
		this.no = new SimpleIntegerProperty(ID);
		this.nullable = new SimpleStringProperty(column.nullable() ? "True" : "False");
	}
}
