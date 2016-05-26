package sqlartan.view.tabs.structureTab;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Projet : SQLartan
 * Créé le 24.05.2016.
 *
 * @author Adriano Ruberto
 */
public abstract class StructureTab {

	private StringProperty name;
	private StringProperty type;

	public StructureTab(String name, String type){
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type);
	}

	public StringProperty nameProperty() {
		return name;
	}
	public StringProperty typeProperty() {
		return type;
	}
}
