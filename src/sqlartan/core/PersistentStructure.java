package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.Lazy;
import java.sql.SQLException;
import java.util.Optional;

/**
 * A persistent structure in a Database.
 * This can be either a Table or a View.
 *
 * @param <T> the actual type of columns of this structure
 */
public abstract class PersistentStructure<T extends Column> implements Structure<T> {
	/**
	 * The parent database of this structure
	 */
	protected Database database;

	/**
	 * The name of the structure
	 */
	protected String name;

	/**
	 * Constructs a new PersistentStructure with the given name.
	 *
	 * @param database the database in which the structure is located
	 * @param name     the name of the structure
	 */
	protected PersistentStructure(Database database, String name) {
		this.database = database;
		this.name = name;
	}

	/**
	 * Returns the name of this structure
	 *
	 * @return the name of this structure
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the database that owns this structure.
	 *
	 * @return the parent database
	 */
	public Database database() {
		return database;
	}

	/**
	 * Returns the safe full name of this persistent structure for use in queries.
	 *
	 * @return the full name of this structure
	 */
	public String fullName() {
		return "[" + database.name() + "].[" + name() + "]";
	}

	/**
	 * Renames this structure.
	 *
	 * The default implementation duplicates the source structure and then
	 * drops the old one. Subclasses may override this default implementation
	 * and use a better mechanism to rename the structure.
	 *
	 * @param target the new name of this structure
	 */
	public void rename(String target) {
		duplicate(target);
		drop();
		name = target;
	}

	/**
	 * Duplicates this structure to a new structure in the same database.
	 *
	 * @param target the name of the copied structure
	 * @return the duplicated structure
	 */
	public abstract PersistentStructure<T> duplicate(String target);

	/**
	 * Drops this structure.
	 */
	public abstract void drop();

	/**
	 * Override in subclasses to build the correct column instance from
	 * a row of the result set of the table_info() pragma.
	 *
	 * @param row the row from table_info()
	 * @return a new instance of a Column sub-type
	 */
	protected abstract T columnBuilder(Row row);

	/**
	 * A lazily computed list of columns in this table
	 */
	private Lazy<ImmutableList<T>> columns = new Lazy<>(() -> {
		try {
			return database.assemble("PRAGMA ", database.name(), ".table_info(", name(), ")")
			               .execute()
			               .map(this::columnBuilder)
			               .toList();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	});

	/**
	 * {@inheritDoc}
	 */
	@Override
	public IterableStream<T> columns() {
		return columns.get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<T> column(String name) {
		return columns().find(col -> col.name().equals(name));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<T> column(int idx) {
		return columns().skip(idx).findFirst();
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
