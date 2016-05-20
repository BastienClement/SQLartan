package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.io.File;
import java.sql.SQLException;

public class AttachedDatabase extends Database {
	private Database main;

	AttachedDatabase(Database main, File path, String name) throws SQLException {
		super(path, name, main.connection);
		this.main = main;
	}

	public Database main() {
		return main;
	}

	void detach() {
		try {
			assemble("DETACH DATABASE ", name()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	@Override
	public void close() {
		// We must not call .close() on the Connection from a child Database
		// Instead detach self
		main.detach(name());
	}
}
