package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;
import java.util.Optional;

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
			throw new RuntimeSQLException(e);
		}
	}

	@Override
	public IterableStream<PersistentStructure<? extends Column>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public IterableStream<GeneratedColumn> columns() {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<GeneratedColumn> column(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public Optional<GeneratedColumn> column(int idx) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
