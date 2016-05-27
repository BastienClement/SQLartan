package sqlartan.view.tabs.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Projet : SQLartan
 * Créé le 25.05.2016.
 *
 * @author Julien Leroy
 * </p>
 * Represent the data to be shown in the the structure tab
 */
public abstract class Model {
	public final StringProperty name;
	public final StringProperty type;

	public Model(String name, String type) {
		this.name = new SimpleStringProperty(name);
		this.type = new SimpleStringProperty(type);
	}
}
