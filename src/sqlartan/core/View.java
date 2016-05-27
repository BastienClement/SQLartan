package sqlartan.core;

import sqlartan.core.ast.CreateViewStatement;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.UncheckedException;
import java.sql.SQLException;
import java.util.Optional;

public class View extends PersistentStructure<GeneratedColumn> implements Structure<GeneratedColumn> {
	protected View(Database database, String name) {
		super(database, name);
	}

	@Override
	public View duplicate(String newName) {
		try {
			// retrieve the view creation sql
			String sql = database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'view' AND name = ?")
			                     .execute(name)
			                     .mapFirst(Row::getString);

			// Modify the create statement
			CreateViewStatement create = Parser.parse(sql, CreateViewStatement::parse);
			create.name = newName;
			create.schema = Optional.of(database.name());

			// Execute the new sql, add the new view
			database.execute(create.toSQL());
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		} catch (ParseException e) {
			throw new UncheckedException(e);
		}

		// Gets the new view, without inspection
		// noinspection OptionalGetWithoutIsPresent
		return database.view(newName).get();
	}

	@Override
	public void drop() {
		try {
			database.assemble("DROP VIEW ", fullName()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	@Override
	protected GeneratedColumn columnBuilder(Row row) {
		// TODO: implements sources
		return new GeneratedColumn(new GeneratedColumn.Properties() {
			public Optional<Table> sourceTable() { return Optional.empty(); }
			public Optional<TableColumn> sourceColumn() { return Optional.empty(); }
			public String name() { return row.getString("name"); }
			public String type() { return row.getString("type"); }
			public boolean nullable() { return row.getInt("notnull") == 0; }
		});
	}
}
