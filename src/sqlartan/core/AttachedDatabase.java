package sqlartan.core;

import sqlartan.core.util.RuntimeSQLException;
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

	public void detach() {
		try {
			main.detach(name());
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	@Override
	public void close() {
		// We must not call .close() on the Connection from a child Database
		// Instead detach self
		try {
			assemble("DETACH DATABASE ", name()).execute();
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}
}
