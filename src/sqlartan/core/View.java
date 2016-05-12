package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;

public class View extends PersistentStructure<GeneratedColumn> implements QueryStructure<GeneratedColumn> {
	protected View(Database database, String name) {
		super(database, name);
	}

	@Override
	public void rename(String newName) {
		try {
			// retrieve the view creation sql
			String sql = database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'view' AND name = ?")
			                     .execute(name)
			                     .mapFirst(Row::getString);

			// Replace the name in the view creation sql
			sql.replaceAll(name, newName);

			// Execute the new sql, add the new view
			database.execute(sql);

			// Delete the old view
			drop();

			// Update view's name
			name = newName;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public View duplicate(String newName) {
		try {
			// retrieve the view creation sql
			String sql = database.assemble("SELECT sql FROM ", database.name(), ".sqlite_master WHERE type = 'view' AND name = ?")
			                     .execute(name)
			                     .mapFirst(Row::getString);

			// Replace the name in the view creation sql
			sql.replaceAll(name, newName);

			// Execute the new sql, add the new view
			database.execute(sql);

			// Gets the new view, without inspection
			return database.view(newName).get();
		} catch (SQLException e) {
			e.printStackTrace();
		}

		throw new UnsupportedOperationException("Not implemented");
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
	public IterableStream<PersistentStructure<? extends Column>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	protected GeneratedColumn columnBuilder(Row row) {
		return new GeneratedColumn(new GeneratedColumn.Properties() {
			public String sourceTable() { throw new UnsupportedOperationException(); }
			public String sourceExpr() { throw new UnsupportedOperationException(); }
			public String name() { return row.getString("name"); }
			public String type() { return row.getString("type"); }
			public boolean nullable() { return row.getInt("notnull") == 0; }
		});
	}
}
