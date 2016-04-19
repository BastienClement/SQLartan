package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class Table extends PersistentStructure<TableColumn> {
	/** Set of indices */
	private HashMap<String, Index> indices = new HashMap<>();

	/** Set of triggers */
	private HashMap<String, Trigger> triggers = new HashMap<>();

	/**
	 * Construct a new table linked to the specified database and with the specified name.
	 *
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

	/**
	 *
	 * @param row
	 * @return
	 */
	private TableColumn columnBuilder(Row row) {
		return new TableColumn(this, new TableColumn.Properties() {
			public String name() { return row.getString("name"); }
			public String type() { return row.getString("type"); }
			public boolean unique() { throw new UnsupportedOperationException("Not implemented"); }
			public String check() { throw new UnsupportedOperationException("Not implemented"); }
		});
	}

	/**
	 * Returns the table_info() pragma result for this table.
	 */
	private Result tableInfo() {
		try {
			String query = database.format("PRAGMA ", database.name(), ".table_info(", name(), ")");
			return database.execute(query);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	@Override
	public IterableStream<TableColumn> columns() {
		return tableInfo().map(this::columnBuilder);
	}

	@Override
	public Optional<TableColumn> column(String name) {
		try (Result res = tableInfo()) {
			return res.find(row -> row.getString("name").equals(name)).map(this::columnBuilder);
		}
	}

	@Override
	public Optional<TableColumn> column(int idx) {
		try (Result res = tableInfo()) {
			return res.skip(idx).mapFirstOptional(this::columnBuilder);
		}
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
	public Index index(String name) { return indices.get(name); }

	/**
	 * Find and return the primaryKey from the indices.
	 *
	 * @return the primary key of the table
	 */
	public Index primaryKey() { throw new UnsupportedOperationException("Not implemented"); }

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
	public Trigger trigger(String name) { return triggers.get(name); }

	/**
	 * Truncate the table.
	 */
	public void truncate() { throw new UnsupportedOperationException("Not implemented"); }
}
