package sqlartan.view.tabs.struct;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Projet : SQLartan
 * Créé le 24.05.2016.
 *
 * @author Adriano Ruberto
 */
public abstract class TabStructure {

	private StringProperty name;
	private StringProperty type;

	public TabStructure(String name, String type){
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
