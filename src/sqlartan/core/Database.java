package sqlartan.core;

import org.sqlite.ExtendedCommand;
import java.io.File;
import java.sql.*;
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

	/** Set of tables */
	private HashMap<String, Table> tables = new HashMap<>();

	/** Set of views */
	private HashMap<String, View> views = new HashMap<>();

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
	 * Returns the hashmap containing every tables.
	 *
	 * @return the hashmap containing the tables
	 */
	public HashMap<String, Table> tables(){ return tables; }

	/**
	 * Returns a table with a specific name.
	 *
	 * @param name
	 * @return the table contained in the hashmap under the key name, null if it doesn't exist
	 */
	public Table table(String name){ return tables.get(name); }

	/**
	 * Returns the hashmap containing every views.
	 *
	 * @return the hashmap containing the views
	 */
	public HashMap<String, View> views(){ return views; }

	/**
	 * Returns a view with a specific name.
	 *
	 * @param name
	 * @return the view contained in the hashmap under the key name, null if it doesn't exist
	 */
	public View view(String name){ return views.get(name); }

	/**
	 * Clean up the database by rebuilding it entirely.
	 *
	 * @throws SQLException
	 */
	public void vacuum() throws SQLException {

	}

	/**
	 * Closes the underlying JDBC Connection object.
	 * Once this method is called, this object must no longer be used.
	 *
	 * Also closes all attached databases.
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

	/**
	 * Executes a query on the database.
	 *
	 * @param query
	 * @return the result of the query
	 * @throws SQLException
	 */
	public Results execute(String query) throws SQLException {
		return new Results(connection, query);
	}

	/**
	 * Executes a query on the database.
	 *
	 * @param query
	 * @return A ResultSet containing the result
	 * @throws SQLException
	 */
	public ResultSet query(String query) throws SQLException {
		return null;
	}

	/**
	 * Prepares a query for execution.
	 *
	 * @param query
	 * @throws SQLException
	 */
	public void prepare(String query) throws SQLException {

	}

	/**
	 * Attaches a database with the name passed in parameters.
	 *
	 * @param name
	 * @return the attached database
	 */
	public AttachedDatabase attach(String name) {
		return null;
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

	}
}
