package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.RuntimeSQLException;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class Database implements AutoCloseable {
	/**
	 * Logical name of this database
	 * This is always "main" for the first database in a SQLite Connection.
	 * Additional databases loaded with ATTACH have user-defined names.
	 */
	private String name;

	/**
	 * File path to this database
	 * For a memory-only database, this is an abstract file named ":memory:".
	 */
	private File path;

	/**
	 * The underlying JDBC connection
	 */
	private Connection connection;

	/**
	 * Set of attached database
	 */
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
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the file path of this database.
	 * If this is a temporary in-memory database, returns an abstract File named ":memory:".
	 */
	public File path() {
		return path;
	}

	/**
	 * Returns a stream of tables in this database.
	 */
	public IterableStream<Table> tables() {
		try {
			String query = format("SELECT name FROM ", name, ".sqlite_master WHERE type = 'table' ORDER BY name ASC");
			return execute(query).map(Row::getString).map(name -> new Table(this, name));
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * Returns the table with the given name, if it exists.
	 * If the table does not exist, an empty Optional is returned.
	 *
	 * @param table the name of the table
	 */
	public Optional<Table> table(String table) {
		try {
			String query = format("SELECT name FROM ", name, ".sqlite_master WHERE type = 'table' AND name = ?");
			return execute(query, table).mapFirstOptional(row -> new Table(this, row.getString()));
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * Returns a stream of views in this database.
	 */
	public IterableStream<View> views() { return null; }

	/**
	 * Returns the view with the given name, if it exists.
	 * If the view does not exist, an empty Optional is returned.
	 *
	 * @param view the name of the view
	 */
	public Optional<View> view(String view) { return null; }

	/**
	 * Clean up the database by rebuilding it entirely.
	 */
	public void vacuum() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Closes the underlying JDBC Connection object.
	 * Once this method is called, this object must no longer be used.
	 *
	 * Also closes all attached databases.
	 *
	 * @throws SQLException
	 */
	public void close() {
		if (this.connection != null) {
			try {
				this.connection.close();
			} catch (SQLException ignored) {}
			this.connection = null;
		}

		attached.values().forEach(AttachedDatabase::close);
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

	/**
	 * TODO
	 * @param parts
	 */
	public String format(String... parts) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i % 2 == 0) {
				sb.append(parts[i]);
			} else {
				sb.append("[");
				sb.append(parts[i]);
				sb.append("]");
			}
		}
		return sb.toString();
	}

	/**
	 * Executes a query on the database.
	 *
	 * @param query
	 * @return the result of the query
	 * @throws SQLException
	 */
	public Result execute(String query) throws SQLException {
		return Result.fromQuery(connection, query);
	}

	/**
	 * Executes a query with placeholders.
	 *
	 * @param query
	 * @param parameters
	 * @return
	 * @throws SQLException
	 */
	public Result execute(String query, Object... parameters) throws SQLException {
		PreparedQuery pq = prepare(query);
		for (int i = 0; i < parameters.length; i++) {
			pq.set(i + 1, parameters[i]);
		}
		return pq.execute();
	}

	/**
	 * Prepares a query for execution.
	 *
	 * @param query
	 * @throws SQLException
	 */
	public PreparedQuery prepare(String query) throws SQLException {
		return new PreparedQuery(connection, query);
	}

	/**
	 * Attaches a database with the name passed in parameters.
	 *
	 * @param name
	 * @return the attached database
	 */
	public AttachedDatabase attach(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Returns the hashmap containing the attachedDatabases.
	 *
	 * @return the attached databases
	 */
	public HashMap<String, AttachedDatabase> attached() {
		return attached;
	}

	/**
	 * Returns a attached database with a specific name.
	 *
	 * @param name
	 * @return the attached database contained in the hashmap under the key name, null if it doesn't exist
	 */
	public AttachedDatabase attached(String name) {
		return attached.get(name);
	}

	/**
	 * Detach an attached database with a specific name.
	 *
	 * @param name
	 */
	public void detach(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}
}
