package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.io.File;
import java.sql.SQLException;

/**
 * Defines a database attached to the main database.
 */
public class AttachedDatabase extends Database {
	/**
	 * The main database
	 */
	private Database main;

	/**
	 * Constructs a new database and attach it to the name database
	 *
	 * @param main  the main database
	 * @param path  the path to the file containing the attached database
	 * @param name  the name of the attached database
	 * @throws SQLException
	 */
	AttachedDatabase(Database main, File path, String name) throws SQLException {
		super(path, name, main.connection);
		this.main = main;
	}

	/**
	 * Get the main database
	 *
	 * @return the main database
	 */
	public Database main() {
		return main;
	}

	/**
	 * Detach the attached database from the main database
	 */
	void detach() {
		try {
			assemble("DETACH DATABASE ", name()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Close the attached database by detaching it
	 */
	@Override
	public void close() {
		// We must not call .close() on the Connection from a child Database
		// Instead detach self
		main.detach(name());
	}
}
