package sqlartan.core;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;

public class Database implements AutoCloseable {
	/**
	 * Logical name of this database.
	 * This is always "main" for the first database in a SQLite Connection.
	 * Additional databases loaded with ATTACH have user-defined names.
	 */
	private String name;

	/**
	 * File path to this database.
	 * For a memory-only database, this is an abstract file named ":memory:".
	 */
	private File path;

	/** The underlying JDBC connection */
	private Connection connection;

	/** Set of attached database */
	private HashMap<String, AttachedDatabase> attached = new HashMap<>();

	/**
	 * Constructs a new memory-only database.
	 *
	 * @throws SQLException
	 */
	public Database() throws SQLException {
		this(":memory:");
	}

	/**
	 * Opens a database file.
	 *
	 * @param path the path to the database file
	 * @throws SQLException
	 */
	public Database(String path) throws SQLException {
		this(new File(path), "main");
	}

	/**
	 * Opens a database file.
	 *
	 * @param path the path to the database file
	 * @throws SQLException
	 */
	public Database(File path) throws SQLException {
		this(path, "main");
	}

	/**
	 * Opens a database file with a specific name.
	 *
	 * @param path the path to the database file
	 * @param name the logical name of the database
	 * @throws SQLException
	 */
	protected Database(File path, String name) throws SQLException {
		this.name = name;
		this.path = path;
		this.connection = DriverManager.getConnection("jdbc:sqlite:" + path.getPath());
	}

	/**
	 * Returns the logical name of this database.
	 * The name of a database opened with the new operator is always "main".
	 * An attached database will have the name given to attach().
	 *
	 * @return the logical name of this database
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the file path of this database.
	 * If this is a temporary in-memory database, returns an abstract File named ":memory:".
	 *
	 * @return the file path of this database
	 */
	public File path() {
		return path;
	}

	/**
	 * Closes the underlying JDBC Connection object.
	 * Once this method is called, this object must no longer be used.
	 *
	 * ALso closes all attached databases.
	 *
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (this.connection != null) {
			this.connection.close();
			this.connection = null;
		}

		for (AttachedDatabase adb : attached.values()) {
			adb.close();
		}

		attached.clear();
	}

	/**
	 * Checks if the underlying JDBC Connection object is closed.
	 * If this is the case, this Database object must no longer be used.
	 *
	 * @return true if the internal JDBC Connection is closed
	 */
	public boolean isClosed() {
		return this.connection == null;
	}

	/**
	 * Checks if this database is a temporary memory-only database.
	 *
	 * @return true if this database is memory-only
	 */
	public boolean isMemoryOnly() {
		return path.getName().equals(":memory:");
	}

	public class AttachedDatabase extends Database {
		protected AttachedDatabase(File path) throws SQLException {
			super(path, name);
			throw new UnsupportedOperationException("Not implemented");
		}

		public Database mainDatabase() {
			return Database.this;
		}
	}
}
