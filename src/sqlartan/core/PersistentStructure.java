package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * TODO
 *
 * @param <T>
 */
public abstract class PersistentStructure<T extends Column> implements Structure<T> {
	protected final Database database;
	protected String name;

	protected PersistentStructure(Database database, String name) {
		this.database = database;
		this.name = name;
	}

	/**
	 * Returns the name of this structure
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the database that owns this structure.
	 */
	public Database database() {
		return database;
	}

	/**
	 * Returns the safe full name of this persistent structure for use in queries.
	 */
	public String fullName() {
		return "[" + database.name() + "].[" + name() + "]";
	}

	/**
	 * TODO
	 *
	 * @param newName
	 */
	public void rename(String newName) {
		duplicate(newName);
		drop();
		name = newName;
	}

	/**
	 * TODO
	 *
	 * @param newName
	 */
	public abstract PersistentStructure<T> duplicate(String newName);

	/**
	 * TODO
	 */
	public abstract void drop();

	/**
	 * Returns the table_info() pragma result for this structure.
	 */
	private Result structureInfo() {
		try {
			return database.assemble("PRAGMA ", database.name(), ".table_info(", name(), ")").execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Override in subclasses to build the correct column instance from
	 * a row of the result set of the table_info() pragma.
	 *
	 * @param row
	 * @return
	 */
	protected abstract T columnBuilder(Row row);

	@Override
	public IterableStream<T> columns() {
		return structureInfo().map(this::columnBuilder);
	}

	@Override
	public Optional<T> column(String name) {
		try (Result res = structureInfo()) {
			return res.find(row -> row.getString("name").equals(name)).map(this::columnBuilder);
		}
	}

	@Override
	public Optional<T> column(int idx) {
		try (Result res = structureInfo()) {
			return res.skip(idx).mapFirstOptional(this::columnBuilder);
		}
	}

	/**
	 * Returns a result set over all entries in the table
	 */
	public Result selectAll() {
		try {
			return database.assemble("SELECT * FROM ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}
}
