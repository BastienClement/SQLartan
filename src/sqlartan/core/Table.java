package sqlartan.core;

import sqlartan.core.alterTable.AlterTable;
import sqlartan.core.ast.ColumnConstraint;
import sqlartan.core.ast.CreateTableStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.UncheckedException;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class Table extends PersistentStructure<TableColumn> {

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
		try {
			database.assemble("ALTER TABLE ", fullName(), " RENAME TO ", newName).execute();
			name = newName;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
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

			// Update the create statement of the original table
			CreateTableStatement create = Parser.parse(createStatement, CreateTableStatement::parse);
			create.name = newName;
			create.schema = Optional.of(database.name());

			// Create the duplicated table
			database.execute(create.toSQL());

			// Insert the data in the table
			database.assemble("INSERT INTO ", database.name(), ".", newName, " SELECT * FROM ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		} catch (ParseException e) {
			throw new UncheckedException(e);
		}

		// noinspection OptionalGetWithoutIsPresent
		return database.table(newName).get();
	}

	/**
	 * Drop the table
	 */
	@Override
	public void drop() {
		try {
			database.assemble("DROP TABLE ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public AlterTable alter(){
		return new AlterTable(this);
	}

	/**
	 *
	 * @param row
	 * @return
	 */
	protected TableColumn columnBuilder(Row row) {
		String createStatement = null;
		try {
			createStatement = database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'table' AND name = ?")
			                                 .execute(name)
			                                 .mapFirst(Row::getString);
			CreateTableStatement create = Parser.parse(createStatement, CreateTableStatement::parse);

			if(create instanceof CreateTableStatement.Def){
				Optional<ColumnConstraint.Check> constraint = ((CreateTableStatement.Def) create).columns.stream().filter(col -> col.name.equals(row.getString("name"))).findFirst().get().constraints.stream().filter(c -> c instanceof ColumnConstraint.Check).map(c -> (ColumnConstraint.Check)c).findFirst();
				if(constraint.isPresent()){
					return new TableColumn(this, new TableColumn.Properties() {
						public String name() { return row.getString("name"); }
						public String type() { return row.getString("type"); }
						public boolean unique() {
							return indices().filter(index -> index.getColumns().stream().filter(name -> name.equals(row.getString("name"))).findFirst().isPresent() && index.isUnique()).findFirst().isPresent();
						}
						@Override
						public boolean primaryKey() {
							return  row.getInt("pk") != 0;
						}
						public String check() { return constraint.get().expression.toSQL(); }
						public boolean nullable() { return row.getInt("notnull") == 0; }
					});
				}
			}
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		} catch (ParseException e) {
			throw new UncheckedSQLException(e);
		}

		// Update the create statement of the original table

		return new TableColumn(this, new TableColumn.Properties() {
			public String name() { return row.getString("name"); }
			public String type() { return row.getString("type"); }
			public boolean unique() {
				return indices().filter(index -> index.getColumns().stream().filter(name -> name.equals(row.getString("name"))).findFirst().isPresent() && index.isUnique()).findFirst().isPresent();
			}
			@Override
			public boolean primaryKey() {
				return  row.getInt("pk") != 0;
			}
			public String check() { return null; }
			public boolean nullable() { return row.getInt("notnull") == 0; }
		});
	}

	/**
	 * Returns the list of indices associated with this table.
	 */
	public IterableStream<Index> indices() {
		try {
			return database.assemble("PRAGMA ", database.name(), ".index_list(", name(), ")")
			               .execute()
			               .map(row -> {
					               Index index = new Index(row.getString("name"), row.getInt("unique") == 1, row.getString(4).equals("pk"));
					               try {
						               database.assemble("PRAGMA ", database.name(), ".index_info(", row.getString("name"), ")")
						                       .execute()
						                       .forEach(
							                       r -> {
								                       index.addColumn(r.getString("name"));
							                       }
						                       );
					               } catch (SQLException e) {
						               throw new UncheckedSQLException(e);
					               }
					               return index;
				               }
			               );
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Returns the index with the given name, if it exists.
	 *
	 * @param name the name of the index
	 */
	public Optional<Index> index(String name) {
		try (IterableStream<Index> indices = indices()) {
			return indices.find(index -> index.getName().equals(name));
		}
	}

	/**
	 * Returns the i-th index from this table, it it exists
	 *
	 * @param idx the index of the index
	 */
	public Optional<Index> index(int idx) {
		try (IterableStream<Index> indices = indices()) {
			return indices.skip(idx).findFirst();
		}
	}

	/**
	 * Find and return the primaryKey from the indices.
	 *
	 * @return the primary key of the table
	 */
	public Optional<Index> primaryKey() {
		// Search in the indices the one which is a primary key
		return indices().filter(index -> index.isPrimaryKey()).findFirst();
	}

	/**
	 * build the correct trigger instance from
	 * a row of the result set
	 *
	 * @param row
	 * @return
	 */
	private Trigger triggerBuilder(Row row){
		return new Trigger(this, row .getString("name"), row.getString("sql"));
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
	 * Add a new column to the table
	 *
	 * @param name
	 * @param affinity
	 */
	@Deprecated
	public void addColumn(String name, Affinity affinity) {
		// TODO
	}

	/**
	 * Inserts a new row in this table
	 */
	public InsertRow insert() {
		return new InsertRow(this);
	}

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

	@Override
	public int hashCode() {
		return Objects.hash(database, name);
	}
}
