package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;

/**
 * A table index.
 */
public class Index {
	/**
	 * Generates the list of indexes for a given table.
	 *
	 * @param table the table
	 * @return an immutable list of indexes
	 */
	static ImmutableList<Index> indicesForTable(Table table) {
		Database database = table.database();
		try {
			return database.assemble("PRAGMA ", database.name(), ".index_list(", table.name(), ")")
			               .execute()
			               .map(row -> new Index(table, row))
			               .toList();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * The table of this index
	 */
	private Table table;

	/**
	 * The name of the index
	 */
	private String name;

	/**
	 * Whether this index is unique or not
	 */
	private boolean unique;

	/**
	 * Whether this index is the table primary key
	 */
	private boolean primaryKey;

	/**
	 * The set of columns composing this index
	 */
	private ImmutableList<String> columns;

	/**
	 * Constructs a new index from a result row.
	 *
	 * @param table the table of this index
	 * @param row   a row of the index_list PRAGMA
	 */
	private Index(Table table, Row row) {
		this.table = table;

		this.name = row.getString("name");
		this.unique = row.getInt("unique") == 1;
		this.primaryKey = row.getString(4).equals("pk");

		try {
			Database database = table.database();
			columns = database.assemble("PRAGMA ", database.name(), ".index_info(", row.getString("name"), ")")
			                  .execute()
			                  .map(r -> r.getString("name"))
			                  .toList();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Returns the table of this index.
	 *
	 * @return the table of this index
	 */
	public Table table() {
		return table;
	}

	/**
	 * Returns this index name.
	 *
	 * @return the index name
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns whether this index is unique or not.
	 *
	 * @return true if the index is unique, false otherwise
	 */
	public boolean unique() {
		return unique;
	}

	/**
	 * Returns whether this index is the table's primary key.
	 *
	 * @return true if the index is the primary key of the table
	 */
	public boolean primaryKey() {
		return primaryKey;
	}

	/**
	 * Returns the set of columns composing this index.
	 *
	 * @return an unmodifiable set of columns composing this index
	 */
	public ImmutableList<String> columns() {
		return columns;
	}
}
