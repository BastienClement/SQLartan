package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.io.File;
import java.sql.SQLException;

/**
 * An attached database.
 */
public class AttachedDatabase extends Database {
	/**
	 * The main database
	 */
	private Database main;

	/**
	 * @param main the main database
	 * @param path the path to the file containing the attached database
	 * @param name the name of the attached database
	 * @throws SQLException
	 */
	AttachedDatabase(Database main, File path, String name) throws SQLException {
		super(path, name, main.connection);
		this.main = main;
	}

	/**
	 * Returns the main database.
	 *
	 * @return the main database
	 */
	public Database main() {
		return main;
	}

	/**
	 * Detaches this attached database from the main database.
	 * <p>
	 * Once detached, queries can not longer be executed on this database.
	 * It is not possible to detach a database with result set still open.
	 */
	void detach() {
		try {
			assemble("DETACH DATABASE ", name()).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Closes the attached database by detaching it.
	 * <p>
	 * The underlying JDBC Connection object is the same for the attached
	 * database and the main one and cannot be closed here. The attached
	 * database is detached instead.
	 * <p>
	 * Once detached, queries can not longer be executed on this database.
	 * It is not possible to detach a database with result set still open.
	 */
	@Override
	public void close() {
		main.detach(name());
	}
}
