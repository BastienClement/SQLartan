package sqlartan.view.tabs.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Table;
import sqlartan.core.View;
import static sqlartan.util.Matching.match;


/**
 * Projet : SQLartan
 * Créé le 17.05.2016.
 *
 * @author Julien Leroy
 *
 * Represent the structure tab of a database
 */
public class DatabaseStructureModel extends Model {
	public final LongProperty lignes;

	public DatabaseStructureModel(PersistentStructure<?> structure) {
		super(structure.name(), match(structure)
			.when(Table.class, t -> "Table")
			.when(View.class, v -> "View")
			.orElse("Unknown"));
		this.lignes = new SimpleLongProperty(structure.selectAll().count());
	}
}
