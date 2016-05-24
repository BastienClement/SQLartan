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
public class DatabaseStructure extends TabStructure {
	private StringProperty lignes;

	public DatabaseStructure(PersistentStructure<?> structure) {
		super(structure.name(), match(structure)
			.when(Table.class, t -> "Table")
			.when(View.class, v -> "View")
			.orElse("Unknown"));
		this.lignes = new SimpleStringProperty("0"); // TODO when nbLignes is done (Bastien will have slept enough)
	}

	public StringProperty lignesProperty() {
		return lignes;
	}
}
