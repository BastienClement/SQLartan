package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.RuntimeSQLException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Database implements AutoCloseable {
	/**
	 * Constructs a new ephemeral database.
	 *
	 * @throws SQLException
	 */
	public static Database createEphemeral() throws SQLException {
		return open(":memory:");
	}

	/**
	 * Opens a database file.
	 *
	 * @param path the path to the database file
	 * @throws SQLException
	 */
	public static Database open(String path) throws SQLException {
		return open(new File(path));
	}

	/**
	 * Opens a database file.
	 *
	 * @param file the path to the database file
	 * @throws SQLException
	 */
	public static Database open(File file) throws SQLException {
		return new Database(file, "main", null);
	}

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
	 * Set of attached database
	 */
	private HashMap<String, AttachedDatabase> attached = new HashMap<>();

	/**
	 * The underlying JDBC connection
	 */
	protected Connection connection;

	/**
	 * @throws SQLException
	 * @deprecated Use Database.createEphemeral() instead
	 */
	@Deprecated
	public Database() throws SQLException {
		this(new File(":memory:"), "main", null);
	}

	/**
	 * @throws SQLException
	 * @deprecated Use Database.open(path) instead
	 */
	@Deprecated
	public Database(String path) throws SQLException {
		this(new File(path), "main", null);
	}

	/**
	 * @throws SQLException
	 * @deprecated Use Database.open(path) instead
	 */
	@Deprecated
	public Database(File path) throws SQLException {
		this(path, "main", null);
	}

	/**
	 * Opens a database file with a specific name.
	 *
	 * @param path       the path to the database file
	 * @param name       the logical name of the database
	 * @param connection the connection to use for this database
	 *                   if null is given a new connection will be created
	 * @throws SQLException
	 */
	protected Database(File path, String name, Connection connection) throws SQLException {
		this.name = name;
		this.path = path;

		if (connection == null) {
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + path.getPath());

			// If the given file is not a database, SQLite will not complain until executing
			// the first query. Do that here so that opening a database triggers an exception
			// if the file is not a database.
			try {
				execute("SELECT * FROM sqlite_master LIMIT 1").close();
			} catch (SQLException e) {
				close();
				throw e;
			}
		} else {
			this.connection = connection;
		}
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
	 * TODO
	 *
	 * @param type
	 * @param builder
	 * @param <T>
	 */
	private <T> IterableStream<T> listStructures(String type, Function<String, T> builder) {
		try {
			return assemble("SELECT name FROM ", name(), ".sqlite_master WHERE type = ? ORDER BY name ASC")
					.execute(type)
					.map(Row::getString)
					.map(builder);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * TODO
	 *
	 * @param type
	 * @param name
	 * @param builder
	 * @param <T>
	 */
	private <T> Optional<T> findStructure(String type, String name, Function<String, T> builder) {
		try {
			return assemble("SELECT name FROM ", name(), ".sqlite_master WHERE type = ? AND name = ?")
					.execute(type, name)
					.mapFirstOptional(Row::getString)
					.map(builder);
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	/**
	 * Returns a stream of tables in this database.
	 */
	public IterableStream<Table> tables() {
		return listStructures("table", n -> new Table(this, n));
	}

	/**
	 * Returns the table with the given name, if it exists.
	 * If the table does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the table
	 */
	public Optional<Table> table(String name) {
		return findStructure("table", name, n -> new Table(this, n));
	}

	/**
	 * Returns a stream of views in this database.
	 */
	public IterableStream<View> views() {
		return listStructures("view", n -> new View(this, n));
	}

	/**
	 * Returns the view with the given name, if it exists.
	 * If the view does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the view
	 */
	public Optional<View> view(String name) {
		return findStructure("view", name, n -> new View(this, n));
	}

	/**
	 * Clean up the database by rebuilding it entirely.
	 */
	public void vacuum() {
		try {
			execute("VACUUM");
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
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
				attached.clear();
				this.connection.close();
			} catch (SQLException ignored) {}
			this.connection = null;
		}
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
	 * @return true if this database is ephemeral
	 */
	public boolean isEphemeral() {
		return path.getName().equals(":memory:");
	}

	/**
	 * @deprecated Use assemble instead
	 */
	@Deprecated
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
	 * TODO
	 *
	 * @param parts
	 */
	public AssembledQuery assemble(String... parts) {
		StringBuilder query = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			if (i % 2 == 0) {
				query.append(parts[i]);
			} else {
				String part = parts[i];
				boolean escaped = part.charAt(0) == '[' && part.charAt(part.length() - 1) == ']';
				if (!escaped) query.append("[");
				query.append(parts[i]);
				if (!escaped) query.append("]");
			}
		}
		return new AssembledQuery(this, query.toString());
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
	 * @param file
	 * @param name
	 * @return the attached database
	 */
	public AttachedDatabase attach(File file, String name) throws SQLException {
		assemble("ATTACH DATABASE ", file.getPath(), " AS ", name).execute();
		AttachedDatabase database = new AttachedDatabase(this, file, name);
		attached.put(name, database);
		return database;
	}

	/**
	 * Attaches a database with the name passed in parameters.
	 *
	 * @param file
	 * @param name
	 * @return the attached database
	 */
	public AttachedDatabase attach(String file, String name) throws SQLException {
		return attach(new File(file), name);
	}

	/**
	 * Returns the hashmap containing the attachedDatabases.
	 *
	 * @return the attached databases
	 */
	public Map<String, AttachedDatabase> attached() {
		return Collections.unmodifiableMap(attached);
	}

	/**
	 * Returns a attached database with a specific name.
	 *
	 * @param name
	 * @return the attached database contained in the hashmap under the key name, null if it doesn't exist
	 */
	public Optional<AttachedDatabase> attached(String name) {
		return Optional.ofNullable(attached.get(name));
	}

	/**
	 * Detach an attached database with a specific name.
	 *
	 * @param name
	 */
	public void detach(String name) {
		AttachedDatabase db = attached(name)
				.orElseThrow(() -> new NoSuchElementException("'" + name + "' is not an attached database"));
		db.detach();
		attached.remove(name);
	}

	/**
	 * Import SQL from a string
	 *
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public Result importFromString(String sql) throws SQLException{
		return execute(sql);
	}

	/**
	 * Import SQL from a file
	 *
	 * @param file
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public Result importfromFile(File file) throws SQLException, IOException{
		return execute(new String(Files.readAllBytes(file.toPath())));
	}

	/**
	 * Export SQL to a String
	 *
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String export() throws SQLException{
		String sql = "";

		// Get every tables
		sql += assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'table'")
				.execute()
				.map(Row::getString)
				.collect(Collectors.joining("\n"));
		sql += "\n";

		// Get every values from tables
		for(Table table : tables()){
			if(assemble("SELECT * FROM ", table.name()).execute().count() > 0) {
				String insertSQL = "INSERT INTO " + table.name() + " VALUES ";
				insertSQL += assemble("SELECT * FROM ", table.name())
						.execute()
						.map(Row::getString)
						.collect(Collectors.joining(", "));

				sql += insertSQL + "\n";
			}
		};

		sql += assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'trigger'")
				.execute()
				.map(Row::getString)
				.collect(Collectors.joining("\n"));
		sql += "\n";

		// Get every views
		sql += assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'view'")
				.execute()
				.map(Row::getString)
				.collect(Collectors.joining("\n"));
		sql += "\n";

		return sql;
	}
}
