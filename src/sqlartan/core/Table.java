package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
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
		try {
			database.assemble("PRAGMA ", database.name(), ".index_list(", name(), ")")
			        .execute().map(Row::view)
			        .forEach(
						row -> {
							try {
								Index index = new Index(row.getString("name"), row.getInt("unique") == 1, row.getString(4).equals("pk"));
								database.assemble("PRAGMA ", database.name(), ".index_info(", row.getString("name"), ")")
								        .execute().map(Row::view)
								        .forEach(
											r -> {
												index.addColumn(r.getString("name"));
											}
										);
								indices.put(row.getString("name"), index);
							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					);
			database.assemble("SELECT name, sql, tbl_name FROM ", database.name(), ".sqlite_master WHERE type = 'trigger' AND tbl_name = ?")
			        .execute(name)
			        .forEach(
					        row -> {
						        triggers.put(row.getString("name"), new Trigger(database, row .getString("name"), row.getString("sql")));
					        }
			        );
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Rename the table to the specified name.
	 *
	 * @param newName
	 */
	@Override
	public void rename(String newName) {
		try {
			String query = "ALTER TABLE " + fullName() + " RENAME TO " + newName;
			database.execute(query);
			name = newName;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * Duplicate the table to a new table with the specified name.
	 * Doesn't duplicate the triggers.
	 *
	 * @param newName
	 */
	@Override
	public Table duplicate(String newName) {
		try {
			// Get the SQL command to create the table
			String createStatement = database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'table' AND name = ?")
			                                 .execute(name)
			                                 .mapFirst(Row::getString);

			// Replace the name in the table
			createStatement = createStatement.replaceFirst(" " + name + " ", " " + newName + " ");

			// Create the new table
			database.execute(createStatement);

			// Insert the data in the table
			database.assemble("INSERT INTO ", newName, "SELECT * FROM ", name).execute();

			//noinspection OptionalGetWithoutIsPresent
			return database.table(newName).get();
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * Drop the table
	 */
	@Override
	public void drop() {
		try {
			String query = "DROP TABLE " + fullName();
			database.execute(query);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
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
			public boolean unique() {
				Iterator<String> keySetIterator = indices.keySet().iterator();
				while(keySetIterator.hasNext()){
					String key = keySetIterator.next();
					if(indices.get(key).getColumns().contains(row.getString("name")) && indices.get(key).isUnique())
						return true;
				}
				return false;
			}
			public String check() { throw new UnsupportedOperationException("Not implemented"); }
			public boolean nullable() { return row.getInt("notnull") == 0; }
		});
	}

	/**
	 * Returns the table_info() pragma result for this table.
	 */
	private Result tableInfo() {
		try {
			return database.assemble("PRAGMA ", database.name(), ".table_info(", name(), ")").execute();
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
	public Index index(String name) {
		if(indices.containsKey(name))
			return indices.get(name);
		return null;
	}

	/**
	 * Find and return the primaryKey from the indices.
	 *
	 * @return the primary key of the table
	 */
	public Index primaryKey() {
		// Search in the indices the one which is a primary key
		Iterator<String> keySetIterator = indices.keySet().iterator();
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			if(indices.get(key).isPrimaryKey())
				return index(key);
		}
		return null;
	}

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
	public Trigger trigger(String name) {
		if(triggers.containsKey(name))
			return triggers.get(name);
		return null;
	}

	/**
	 * Truncate the table.
	 */
	public void truncate() {
		try {
			String query = "DELETE FROM " + fullName();
			database.execute(query);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}
}
