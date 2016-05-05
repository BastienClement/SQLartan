package sqlartan.view.tabs.struct;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Table;
import sqlartan.core.View;
import static sqlartan.util.Matching.match;

/**
 * Projet : SQLartan
 * Créé le 03.05.2016.
 *
 * @author Adriano Ruberto
 *
 * Represente the structure tab for a database.
 */
public class DatabaseStructure {
	private StringProperty name;
	private StringProperty type;
	private StringProperty lignes;
	private StringProperty actions;

	public DatabaseStructure(PersistentStructure<?> structure) {
		this.name = new SimpleStringProperty(structure.name());
		this.type = new SimpleStringProperty(match(structure)
				.when(Table.class, t -> "Table")
				.when(View.class, v -> "View")
				.orElse("Unknown"));
		this.lignes = new SimpleStringProperty("0"); // TODO when nbLignes is done (Bastien will have slept enough)
	}

	public StringProperty nameProperty() {
		return name;
	}

	public StringProperty typeProperty() {
		return type;
	}

	public StringProperty lignesProperty() {
		return lignes;
	}

	public StringProperty actionsProperty() {
		return actions;
	}
}
