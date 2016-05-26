package sqlartan.view.tabs.structureTab;

import javafx.beans.property.*;
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
public class DatabaseStructureTab extends StructureTab {
	private LongProperty lignes;

	public DatabaseStructureTab(PersistentStructure<?> structure) {
		super(structure.name(), match(structure)
			.when(Table.class, t -> "Table")
			.when(View.class, v -> "View")
			.orElse("Unknown"));
		this.lignes = new SimpleLongProperty(structure.selectAll().count());
	}

	public LongProperty lignesProperty() {
		return lignes;
	}
}
