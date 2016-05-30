package sqlartan.gui.controller.tabs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The data model to be shown in the the structure tab.
 */
public abstract class StructureModel {
	public final StringProperty name;
	public final StringProperty type;

	public StructureModel(String name, String type) {
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type);
	}
}
