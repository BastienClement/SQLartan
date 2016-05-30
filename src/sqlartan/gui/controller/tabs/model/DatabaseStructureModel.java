package sqlartan.gui.controller.tabs.model;

import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import sqlartan.core.PersistentStructure;
import sqlartan.core.Table;
import sqlartan.core.View;
import static sqlartan.util.Matching.match;

/**
 * Represent the model of the structure tab of a database.
 */
public class DatabaseStructureModel extends StructureModel {
	public final LongProperty lines;

	public DatabaseStructureModel(PersistentStructure<?> structure) {
		super(structure.name(), match(structure)
			.when(Table.class, t -> "Table")
			.when(View.class, v -> "View")
			.orElse("Unknown"));
		this.lines = new SimpleLongProperty(structure.selectAll().count());
	}
}
