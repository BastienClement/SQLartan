package sqlartan.core;

import sqlartan.core.ast.token.Token;
import sqlartan.core.ast.token.TokenSource;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.Optionals;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import static sqlartan.core.ast.Keyword.*;
import static sqlartan.core.ast.Operator.SEMICOLON;
import static sqlartan.util.Matching.match;

/**
 * An SQLite database.
 */
public class Database implements AutoCloseable {
	/**
	 * Creates a new ephemeral database.
	 * <p>
	 * Ephemeral databases are in-memory databases with no associated data
	 * file. Once closed, their content is lost.
	 * <p>
	 * In SQLite, this is achieved by using ':memory:' as the file path when
	 * opening a database.
	 *
	 * @throws SQLException if an error occurs while creating the database
	 */
	public static Database createEphemeral() throws SQLException {
		return open(":memory:");
	}

	/**
	 * Opens a database from a file path.
	 * <p>
	 * If the file does not exists, it is created and the database will be
	 * empty. If the file is not a valid SQLite3 database, an exception
	 * is thrown.
	 *
	 * @param path the path to the database file
	 * @throws SQLException if an error occurs while opening the database
	 *                      or if the given file is not a valid SQLite3
	 *                      database.
	 */
	public static Database open(String path) throws SQLException {
		return open(new File(path));
	}

	/**
	 * Opens a database from a file.
	 * <p>
	 * If the file does not exists, it is created and the database will be
	 * empty. If the file is not a valid SQLite3 database, an exception
	 * is thrown.
	 *
	 * @param file the database file
	 * @throws SQLException if an error occurs while opening the database
	 *                      or if the given file is not a valid SQLite3
	 *                      database.
	 */
	public static Database open(File file) throws SQLException {
		return new Database(file, "main", null);
	}

	/**
	 * The logical name of this database
	 * This is always "main" for the first database.
	 * Additional databases opened with ATTACH have user-defined names.
	 */
	private String name;

	/**
	 * The file path to this database
	 * For a memory-only database, this is an abstract file named ':memory:'.
	 */
	private File path;

	/**
	 * The Set of attached database
	 */
	private HashMap<String, AttachedDatabase> attached = new HashMap<>();

	/**
	 * The underlying JDBC connection
	 */
	protected Connection connection;

	/**
	 * The set of registered execute listeners
	 */
	private Set<Consumer<ReadOnlyResult>> executeListeners = new HashSet<>();

	/**
	 * @param path       the path to the database file
	 * @param name       the logical name of the database
	 * @param connection the connection to use for this database, if null is
	 *                   given a new connection will be created
	 * @throws SQLException if an error occurs while opening the database
	 *                      or if the given file is not a valid SQLite3
	 *                      database.
	 */
	protected Database(File path, String name, Connection connection) throws SQLException {
		this.name = name;
		this.path = path;

		if (connection == null) {
			this.connection = DriverManager.getConnection("jdbc:sqlite:" + path.getPath());

			// If the given file is not a database, SQLite will not complain
			// until executing the first query. Do that here so that opening
			// a database triggers an exception if the file is not a database.
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
	 * <p>
	 * The name of a database opened with the static methods from this class
	 * is always "main". An attached database will have the name given to
	 * attach().
	 *
	 * @return the name of the database
	 */
	public String name() {
		return name;
	}

	/**
	 * Returns the file path of this database.
	 * <p>
	 * If this is a temporary in-memory database, returns an abstract File
	 * named ":memory:".
	 *
	 * @return the file containing the database
	 */
	public File path() {
		return path;
	}

	/**
	 * Registers a new execute listener.
	 * <p>
	 * Executes listeners are called each time a query is executed on the
	 * database. They receive a read only view of the result, allowing them
	 * to retrieve the source SQL query and updated row count, but not consume
	 * the results themselves.
	 *
	 * @param listener a function to call when executing a query on the
	 *                 database
	 */
	public void registerListener(Consumer<ReadOnlyResult> listener) {
		executeListeners.add(listener);
	}

	/**
	 * Removes a registered listener from the database.
	 *
	 * @param listener the callback function, previously given to register,
	 *                 to remove.
	 */
	public void removeListener(Consumer<ReadOnlyResult> listener) {
		executeListeners.remove(listener);
	}

	/**
	 * Returns a list of structures of the given type from this database.
	 * <p>
	 * Used to list Tables and Views from this database.
	 *
	 * @param type    the type of structure to list
	 * @param builder the builder function, constructing a concrete instance
	 *                of the structure from its name
	 * @param <T>     the class of the structures in the list
	 * @return a list of structure from this table
	 */
	private <T> IterableStream<T> listStructures(String type, Function<String, T> builder) {
		try {
			return assemble("SELECT name FROM ", name(), ".sqlite_master WHERE type = ? ORDER BY name ASC")
				.execute(type)
				.map(Row::getString)
				.map(builder);
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Finds a structure with the given type and name in this database.
	 *
	 * @param type    the type of the structure
	 * @param name    the name of the structure
	 * @param builder the builder function, constructing a concrete instance
	 *                of the structure from its name
	 * @param <T>     the class of the structure
	 * @return the matching structure, an empty optional if not found
	 */
	private <T> Optional<T> findStructure(String type, String name, Function<String, T> builder) {
		try {
			return assemble("SELECT name FROM ", name(), ".sqlite_master WHERE type = ? AND name = ?")
				.execute(type, name)
				.mapFirstOptional(Row::getString)
				.map(builder);
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Returns a stream of every tables in this database.
	 *
	 * @return a stream of tables
	 */
	public IterableStream<Table> tables() {
		return listStructures("table", n -> new Table(this, n));
	}

	/**
	 * Returns the table with the given name, if it exists.
	 * If the table does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the table
	 * @return the table with the given name, if it exists
	 */
	public Optional<Table> table(String name) {
		return findStructure("table", name, n -> new Table(this, n));
	}

	/**
	 * Creates a new empty table.
	 *
	 * @param name the name of the new table
	 * @throws SQLException
	 */
	public Table createTable(String name) throws SQLException {
		assemble("CREATE TABLE ", name(), ".", name, " (id INTEGER PRIMARY KEY)").execute();
		return table(name).orElseThrow(IllegalStateException::new);
	}

	/**
	 * Returns a stream of every views in the database.
	 *
	 * @return a stream of views
	 */
	public IterableStream<View> views() {
		return listStructures("view", n -> new View(this, n));
	}

	/**
	 * Returns the view with the given name, if it exists.
	 * If the view does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the view
	 * @return the view with the given name, if it exists
	 */
	public Optional<View> view(String name) {
		return findStructure("view", name, n -> new View(this, n));
	}

	/**
	 * Returns a list of every persistent structures of the database.
	 * <p>
	 * This method returns the concatenation of the tables() and views()
	 * streams.
	 *
	 * @return a stream of persistent structures
	 */
	public IterableStream<PersistentStructure<? extends Column>> structures() {
		return IterableStream.concat(tables(), views());
	}

	/**
	 * Returns the structure with the given name, if it exists.
	 * If the structure does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the structure
	 * @return the optional structure
	 */
	public Optional<PersistentStructure<? extends Column>> structure(String name) {
		return Optionals.firstPresent(() -> table(name), () -> view(name));
	}

	/**
	 * Performs a VACUUM operation on the database, rebuilding it entirely.
	 */
	public void vacuum() {
		try {
			execute("VACUUM");
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Closes the underlying JDBC Connection object.
	 * Once this method is called, this object must no longer be used.
	 * <p>
	 * Also closes all attached databases.
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
	 * Assembles the given query fragments by escaping every even-position
	 * argument and concatenating them.
	 *
	 * @param parts the query fragments
	 * @return an assembled query bound to this database
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
	 * Notifies registered execute listeners.
	 * <p>
	 * This method returns the given result set allowing it to be used in a
	 * return statement without requiring an additional variable to hold the
	 * result set.
	 *
	 * @param res the result set generated by the executed query.
	 * @return the given result set
	 */
	Result notifyListeners(Result res) {
		for (Consumer<ReadOnlyResult> listener : executeListeners) {
			try {
				listener.accept(res);
			} catch (Throwable ignored) {}
		}
		return res;
	}

	/**
	 * Executes a query on the database.
	 *
	 * @param query the SQL query to execute
	 * @return the result set of the query
	 * @throws SQLException if the query is invalid
	 */
	public Result execute(String query) throws SQLException {
		return notifyListeners(Result.fromQuery(this, connection, query));
	}

	/**
	 * Executes a query containing multiple statements on the database.
	 * <p>
	 * The JDBC SQLite driver does not support executing a query composed
	 * of multiple statement. This method handle the task of splitting the
	 * query into independent statement that can be executed one by one
	 * on the database.
	 * <p>
	 * These statements are executed independently, this method does not
	 * start a transaction.
	 * <p>
	 * This methods returns a stream of result set instances. Care must be
	 * taken to properly close each of theses result set to prevent a
	 * resource leak.
	 *
	 * @param query the multiple-statement query to execute
	 * @return a stream of results
	 * @throws SQLException if the query is invalid
	 */
	public IterableStream<Result> executeMulti(String query) throws SQLException, TokenizeException {
		TokenSource tokens = TokenSource.from(query);
		return IterableStream.from(() -> {
			return new Iterator<Result>() {
				private int begin = 0;
				private int len = query.length();
				private String statement;

				// Initialization
				{ findStatement(); }

				@SuppressWarnings("EqualsBetweenInconvertibleTypes")
				private void findStatement() {
					if (begin >= len) {
						statement = null;
						return;
					}

					int block_level = 0;
					for (Token current = tokens.current(); ; tokens.consume(), current = tokens.current()) {
						if ((current.equals(BEGIN) && !tokens.next().equals(TRANSACTION)) || current.equals(MATCH)) {
							block_level++;
						} else if (current.equals(END)) {
							block_level--;
						} else if (current instanceof Token.EndOfStream) {
							statement = query.substring(begin).trim();
							begin = len;
							break;
						} else if (block_level == 0 && current.equals(SEMICOLON)) {
							int offset = current.offset + 1;
							statement = query.substring(begin, offset).trim();
							begin = offset;
							tokens.consume();
							break;
						}
					}

					if (statement.isEmpty()) {
						findStatement();
					}
				}

				@Override
				public boolean hasNext() {
					return statement != null;
				}

				@Override
				public Result next() {
					try {
						return execute(statement);
					} catch (SQLException e) {
						throw new UncheckedSQLException(e);
					} finally {
						findStatement();
					}
				}
			};
		});
	}

	/**
	 * Executes a query with placeholders.
	 *
	 * @param query      the SQL query to execute
	 * @param parameters a list of values for the placeholders
	 * @return the result set generated by the query
	 * @throws SQLException if the query is invalid
	 */
	public Result execute(String query, Object... parameters) throws SQLException {
		PreparedQuery pq = prepare(query);
		for (int i = 0; i < parameters.length; i++) {
			pq.set(i + 1, parameters[i]);
		}
		return pq.execute();
	}

	/**
	 * Executes a transaction on the database.
	 *
	 * @param queries
	 * @throws SQLException
	 */
	public void executeTransaction(String[] queries) throws SQLException {
		try {
			connection.setAutoCommit(false);
			for (String query : queries) {
				execute(query).close();
			}
			connection.commit();
		} catch (SQLException e) {
			connection.rollback();
			throw e;
		} finally {
			connection.setAutoCommit(true);
		}
	}

	/**
	 * Prepares a query for execution.
	 *
	 * @param query the query to execute, can contain placeholders
	 * @throws SQLException if the query is invalid
	 */
	public PreparedQuery prepare(String query) throws SQLException {
		return new PreparedQuery(this, connection, query);
	}

	/**
	 * Attaches a database to this one.
	 *
	 * @param file the database file to open
	 * @param name the logical name of the attached database
	 * @return the attached database
	 */
	public AttachedDatabase attach(File file, String name) throws SQLException {
		assemble("ATTACH DATABASE ", file.getPath(), " AS ", name).execute();
		AttachedDatabase database = new AttachedDatabase(this, file, name);
		attached.put(name, database);
		return database;
	}

	/**
	 * Attaches a database to this one.
	 *
	 * @param file the database file path to open
	 * @param name the logical name of the attached database
	 * @return the attached database
	 */
	public AttachedDatabase attach(String file, String name) throws SQLException {
		return attach(new File(file), name);
	}

	/**
	 * Returns an unmodifiable map of every attached database.
	 *
	 * @return the attached databases
	 */
	public Map<String, AttachedDatabase> attached() {
		return Collections.unmodifiableMap(attached);
	}

	/**
	 * Returns the attached database with a specific name.
	 *
	 * @param name the attached database name
	 * @return the attached database with the requested name, an empty
	 * optional if it does not exist.
	 */
	public Optional<AttachedDatabase> attached(String name) {
		return Optional.ofNullable(attached.get(name));
	}

	/**
	 * Detaches the attached database with a specific name.
	 *
	 * @param name the name of the attached database to detach
	 * @throws NoSuchElementException if there is no attached database
	 *                                with the given name
	 */
	public void detach(String name) {
		attached(name).orElseThrow(NoSuchElementException::new).detach();
		attached.remove(name);
	}

	/**
	 * Import SQL from a string.
	 *
	 * @param sql the String containing the SQL
	 * @throws SQLException
	 */
	public void importFromString(String sql) throws SQLException, TokenizeException {
		executeMulti(sql).forEach(Result::close);
	}

	/**
	 * Import SQL from a file.
	 *
	 * @param file the file containing the SQL
	 * @throws SQLException
	 * @throws IOException
	 */

	public void importFromFile(File file) throws SQLException, IOException, TokenizeException {
		executeMulti(new String(Files.readAllBytes(file.toPath()))).forEach(Result::close);
	}

	/**
	 * Export the database to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 * @throws IOException
	 */
	public String export() throws SQLException {
		return createSQLTransaction(getTablesSQL() + getTablesDataSQL() + getViewsSQL() + getTriggersSQL());
	}

	/**
	 * Export tables data to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 * @throws IOException
	 */
	public String exportTablesData() throws SQLException {
		return createSQLTransaction(getTablesDataSQL());
	}

	/**
	 * Export the structure of the database to SQL
	 *
	 * @return the SQL
	 * @throws SQLException
	 * @throws IOException
	 */
	public String exportStructure() throws SQLException {
		return createSQLTransaction(getTablesSQL() + getViewsSQL() + getTriggersSQL());
	}

	/**
	 * Put SQL inside a transaction.
	 *
	 * @param sql
	 * @return the transaction
	 */
	private String createSQLTransaction(String sql) {
		return "PRAGMA foreign_keys=OFF;\n" +
			"BEGIN TRANSACTION;\n" +
			sql +
			"COMMIT;";
	}

	/**
	 * Export tables to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 */
	private String getTablesSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'table'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export views to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 */
	private String getViewsSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'view'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export triggers to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 */
	private String getTriggersSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'trigger'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export tables data to SQL.
	 *
	 * @return the SQL
	 * @throws SQLException
	 */
	private String getTablesDataSQL() throws SQLException {
		String sql = "";

		// Get every values from tables
		for (Table table : tables()) {
			if (assemble("SELECT COUNT(*) FROM ", table.fullName()).execute().mapFirst(Row::getInt) > 0) {
				String insertSQL = "INSERT INTO " + table.fullName() + " VALUES ";
				insertSQL += assemble("SELECT * FROM ", table.fullName())
					.execute()
					.map(row -> {
						String s = "(";
						for (int i = 1; i <= row.size(); i++) {
							if (i != 1) s += ", ";
							s += match(row.getObject(i))
								.when(String.class, str -> "'" + str.replace("'", "''") + "'")
								.when(Number.class, n -> n.toString())
								.orElse(() -> {
									return "NULL";
								});
							// TODO Manage byte array
						}
						return s + ")";
					})
					.collect(Collectors.joining(", "));

				sql += insertSQL + ";\n";
			}
		}

		return sql;
	}
}
