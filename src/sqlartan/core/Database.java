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

public class Database implements AutoCloseable {
	/**
	 * The underlying JDBC connection
	 */
	protected Connection connection;
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
	 * The set of registered execute listeners
	 */
	private Set<Consumer<ReadOnlyResult>> executeListeners = new HashSet<>();

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
	 * @param listener
	 */
	public void registerListener(Consumer<ReadOnlyResult> listener) {
		executeListeners.add(listener);
	}

	/**
	 * TODO
	 *
	 * @param listener
	 */
	public void removeListener(Consumer<ReadOnlyResult> listener) {
		executeListeners.remove(listener);
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
			throw new UncheckedSQLException(e);
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
			throw new UncheckedSQLException(e);
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
	 * Add a new empty table
	 *
	 * @param name
	 * @throws SQLException
	 */
	public void addTable(String name) throws SQLException {
		assemble("CREATE TABLE ", name, "(id INTEGER PRIMARY KEY);").execute();
	}

	/**
	 * Returns a stream of views in this database.
	 */
	public IterableStream<View> views() {
		return listStructures("gui", n -> new View(this, n));
	}

	/**
	 * Returns the gui with the given name, if it exists.
	 * If the gui does not exist, an empty Optional is returned.
	 *
	 * @param name the name of the gui
	 */
	public Optional<View> view(String name) {
		return findStructure("gui", name, n -> new View(this, n));
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public IterableStream<PersistentStructure<? extends Column>> structures() {
		return IterableStream.concat(tables(), views());
	}

	/**
	 * TODO
	 *
	 * @param name
	 * @return
	 */
	public Optional<PersistentStructure<? extends Column>> structure(String name) {
		return Optionals.firstPresent(() -> table(name), () -> view(name));
	}

	/**
	 * Clean up the database by rebuilding it entirely.
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

	Result notifyListeners(String query, Result res) {
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
	 * @param query
	 * @return the result of the query
	 * @throws SQLException
	 */
	public Result execute(String query) throws SQLException {
		return notifyListeners(query, Result.fromQuery(this, connection, query));
	}

	/**
	 * @param query
	 * @return
	 * @throws SQLException
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
	 * @param query
	 * @throws SQLException
	 */
	public PreparedQuery prepare(String query) throws SQLException {
		return new PreparedQuery(this, connection, query);
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
	public void importFromString(String sql) throws SQLException, TokenizeException {
		executeMulti(sql).forEach(Result::close);
	}

	/**
	 * Import SQL from a file
	 *
	 * @param file
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */

	public void importfromFile(File file) throws SQLException, IOException, TokenizeException {
		executeMulti(new String(Files.readAllBytes(file.toPath()))).forEach(Result::close);
	}

	/**
	 * Export db to SQL
	 *
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String export() throws SQLException {
		return createSQLTransaction(getTablesSQL() + getTablesDataSQL() + getViewsSQL() + getTriggersSQL());
	}

	/**
	 * Export Tables data to SQL
	 *
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String exportTablesData() throws SQLException {
		return createSQLTransaction(getTablesDataSQL());
	}

	/**
	 * Export structure to SQL
	 *
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public String exportStructure() throws SQLException {
		return createSQLTransaction(getTablesSQL() + getViewsSQL() + getTriggersSQL());
	}

	private String createSQLTransaction(String sql) {
		return "PRAGMA foreign_keys=OFF;\n" +
			"BEGIN TRANSACTION;\n" +
			sql +
			"COMMIT;";
	}

	/**
	 * Export table to SQL
	 *
	 * @return
	 * @throws SQLException
	 */
	private String getTablesSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'table'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export views to SQL
	 *
	 * @return
	 * @throws SQLException
	 */
	private String getViewsSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'view'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export triggers to SQL
	 *
	 * @return
	 * @throws SQLException
	 */
	private String getTriggersSQL() throws SQLException {
		return assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'trigger'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n")) + ";\n";
	}

	/**
	 * Export tables data to SQL
	 *
	 * @return
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

		// Get every views
		sql += assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'gui'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n"));
		sql += ";\n";

		// Get every triggers
		sql += assemble("SELECT sql FROM ", name, ".sqlite_master WHERE type = 'trigger'")
			.execute()
			.map(Row::getString)
			.collect(Collectors.joining(";\n"));
		sql += ";\n";

		return sql;
	}
}
