package sqlartan.core;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Table extends PersistentStructure<TableColumn> {

	/** Set of indices */
	private HashMap<String, Index> indices = new HashMap<>();

	/** Set of triggers */
	private HashMap<String, Trigger> triggers = new HashMap<>();

	/**
	 * Construct a new table linked to the specified database and with the specified name.
	 * @param database
	 * @param name
	 */
	Table(Database database, String name) {
		super(database, name);
	}

	/**
	 * Rename the table to the specified name.
	 *
	 * @param newName
	 */
	@Override
	public void rename(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Duplicate the table to a new table with the specified name.
	 *
	 * @param newName
	 */
	@Override
	public void duplicate(String newName) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Drop the table
	 */
	@Override
	public void drop() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public List<TableColumn> columns() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public int columnCount() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<TableColumn> column(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<TableColumn> column(int idx) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Returns the hashmap containing every indices.
	 *
	 * @return the hashmap containing the indices
	 */
	public HashMap<String, Index> indices() { return indices; }

	/**
	 * Returns an index with a specific name.
	 *
	 * @param name
	 * @return the index contained in the hashmap under the key name, null if it doesn't exist
	 */
	public Index index(String name){ return indices.get(name); }

	/**
	 * Find and return the primaryKey from the indices.
	 *
	 * @return the primary key of the table
	 */
	public Index primaryKey(){ throw new UnsupportedOperationException("Not implemented"); }

	/**
	 * Returns the hashmap containing every triggers.
	 *
	 * @return the hashmap containing the triggers
	 */
	public HashMap<String, Trigger> triggers() { return triggers; }

	/**
	 * Returns a trigger with a specific name.
	 *
	 * @param name
	 * @return the trigger contained in the hashmap under the key name, null if it doesn't exist
	 */
	public Trigger trigger(String name){ return triggers.get(name); }

	/**
	 * Truncate the table.
	 */
	public void truncate(){ throw new UnsupportedOperationException("Not implemented"); }
}
