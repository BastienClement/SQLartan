package sqlartan.core;

import sqlartan.core.alter.AlterTable;
import sqlartan.core.ast.ColumnConstraint;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.Lazy;
import sqlartan.util.UncheckedException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import static sqlartan.util.Matching.match;

/**
 * A table in a database
 */
public class Table extends PersistentStructure<TableColumn> {
	/**
	 * Constructs a new table linked to the specified database and with
	 * the specified name.
	 *
	 * @param database the parent database
	 * @param name     the name of this table
	 */
	Table(Database database, String name) {
		super(database, name);
	}

	/**
	 * The CREATE TABLE statement corresponding to this table
	 */
	private Lazy<String> createStatement = new Lazy<>(() -> {
		try {
			return database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'table' AND name = ?")
			               .execute(this.name)
			               .mapFirst(Row::getString);
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	});

	/**
	 * The parsed CREATE TABLE statement corresponding to this table
	 */
	private Lazy<CreateTableStatement> createStatementParsed = new Lazy<>(() -> {
		try {
			return Parser.parse(createStatement.get(), CreateTableStatement::parse);
		} catch (ParseException e) {
			throw new UncheckedException(e);
		}
	});

	/**
	 * Renames the table to the specified name.
	 *
	 * @param target the new name of the table
	 */
	@Override
	public void rename(String target) {
		try {
			database.assemble("ALTER TABLE ", fullName(), " RENAME TO ", target).execute();
			name = target;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Duplicates the table to a new table with the specified name.
	 * Does not duplicate associated triggers.
	 *
	 * @param target the name
	 * @return the new table
	 */
	@Override
	public Table duplicate(String target) {
		try {
			// Update the create statement of the original table
			CreateTableStatement create = createStatementParsed.gen();
			create.name = target;
			create.schema = Optional.of(database.name());

			// Create the duplicated table
			database.execute(create.toSQL());

			// Insert the data in the table
			database.assemble("INSERT INTO ", database.name(), ".", target, " SELECT * FROM ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}

		// noinspection OptionalGetWithoutIsPresent
		return database.table(target).get();
	}

	/**
	 * Drops the table.
	 */
	@Override
	public void drop() {
		try {
			database.assemble("DROP TABLE ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Constructs an AlterTable object to alter the structure of this table.
	 *
	 * @return a new AlterTable object bound to this table
	 */
	public AlterTable alter() {
		return new AlterTable(this);
	}

	/**
	 * Builds a TableColumn for a given Row of the table_info PRAGMA result.
	 *
	 * @param row a row from a PRAGMA table_info query
	 * @return a TableColumn representing the column described by the row
	 */
	protected TableColumn columnBuilder(Row row) {
		return match(createStatementParsed.get())
			.when(CreateTableStatement.Def.class, def -> {
				String columnName = row.getString("name");
				String columnType = row.getString("type");
				boolean columnPrimaryKey = row.getInt("pk") != 0;
				boolean columnNullable = row.getInt("notnull") == 0;

				return def.columns.stream().filter(c -> c.name.equals(columnName)).findFirst().map(col -> {
					// The check constraint on this column
					Optional<ColumnConstraint.Check> check =
						col.constraints.stream()
						               .filter(c -> c instanceof ColumnConstraint.Check)
						               .map(c -> (ColumnConstraint.Check) c).findFirst();

					String columnCheck = check.map(c -> c.expression.toSQL()).orElse(null);

					// Whether this column is unique or not
					Lazy<Boolean> columnUnique = new Lazy<>(
						() -> indices().filter(i -> i.unique() && i.columns().contains(columnName)).exists()
					);

					return new TableColumn(this, new TableColumn.Properties() {
						@Override
						public String name() { return columnName; }
						@Override
						public String type() { return columnType; }
						@Override
						public boolean unique() { return columnUnique.get(); }
						@Override
						public boolean primaryKey() { return columnPrimaryKey; }
						@Override
						public String check() { return columnCheck; }
						@Override
						public boolean nullable() { return columnNullable;}
					});
				}).orElseThrow(IllegalStateException::new);
			}).orElseThrow(UnsupportedOperationException::new);
	}

	/**
	 * The indices defined for this table
	 */
	private Lazy<ImmutableList<Index>> indices = new Lazy<>(() -> Index.indicesForTable(this));

	/**
	 * Returns the list of indices associated with this table.
	 *
	 * @return the list of indices for this table
	 */
	public ImmutableList<Index> indices() {
		return indices.get();
	}

	/**
	 * Returns the index with the given name.
	 *
	 * @param name the name of the index
	 * @return the index with the given name, if it exists
	 */
	public Optional<Index> index(String name) {
		return indices().find(index -> index.name().equals(name));
	}

	/**
	 * Returns the i-th index for this table.
	 *
	 * @param idx the index of the index
	 * @return the i-th index for this table, if it exists
	 */
	public Optional<Index> index(int idx) {
		return indices().skip(idx).findFirst();
	}

	/**
	 * Returns the primary key index for this table.
	 *
	 * @return the primary key index of the table
	 */
	public Optional<Index> primaryKey() {
		return indices().find(Index::primaryKey);
	}

	/**
	 * build the correct trigger instance from
	 * a row of the result set
	 *
	 * @param row
	 * @return
	 */
	private Trigger triggerBuilder(Row row) {
		return new Trigger(this, row.getString("name"), row.getString("sql"));
	}

	/**
	 * Returns the triggers infos result for this table.
	 */
	private Result triggersInfo() {
		try {
			return database.assemble("SELECT name, sql, tbl_name FROM ", database.name(), ".sqlite_master WHERE type = 'trigger' AND tbl_name = ?").execute(name);
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Returns the list of trigggers associated with this table.
	 */
	public IterableStream<Trigger> triggers() {
		return triggersInfo().map(this::triggerBuilder);
	}

	/**
	 * Returns the trigger with the given name, if it exists.
	 *
	 * @param name the name of the trigger
	 */
	public Optional<Trigger> trigger(String name) {
		try (Result res = triggersInfo()) {
			return res.find(row -> row.getString("name").equals(name)).map(this::triggerBuilder);
		}
	}

	/**
	 * Returns the i-th trigger from this table, it it exists
	 *
	 * @param idx the index of the trigger
	 */
	public Optional<Trigger> trigger(int idx) {
		try (Result res = triggersInfo()) {
			return res.skip(idx).mapFirstOptional(this::triggerBuilder);
		}
	}

	/**
	 * Truncate the table.
	 */
	public void truncate() {
		try {
			String query = "DELETE FROM " + fullName();
			database.execute(query);
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Inserts new data in this table.
	 *
	 * @return a instance of InsertRow allowing to add data in this table
	 */
	public InsertRow insert() {
		return new InsertRow(this);
	}

	/**
	 * Two tables are equals if they both have the same parent database and
	 * the same name.
	 *
	 * @param obj the object to test for equality
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Table) {
			Table table = (Table) obj;
			return database == table.database && name.equals(table.name);
		} else {
			return false;
		}
	}

	/**
	 * Generates a hash code for this table.
	 * Since tables can be renamed, we can not use its name for hash code
	 * computation. This means that every table in the same database will
	 * have the same hashcode and make very bad keys in hash structures.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(database, Table.class);
	}
}
