package sqlartan.core;

import java.sql.SQLException;

/**
 * Defines an assembled query for executing SQLITE on a database
 */
public class AssembledQuery {
	/**
	 * The database on which the queries will be executed
	 */
	private final Database database;

	/**
	 * The String containing the query
	 */
	private final String query;

	/**
	 * Construct a new assembled query for the specified database and with the given String.
	 *
	 * @param database  The database on which the queries will be executed
	 * @param query     The String containing the query
	 */
	AssembledQuery(Database database, String query) {
		this.database = database;
		this.query = query;
	}

	/**
	 * Get the SQL of the query.
	 *
	 * @return the String containing the SQL
	 */
	public String query() {
		return query;
	}

	/**
	 * Prepare the query for the execution.
	 *
	 * @return The prepared query
	 * @throws SQLException
	 */
	public PreparedQuery prepare() throws SQLException {
		return database.prepare(query);
	}

	/**
	 * Executes the query.
	 *
	 * @throws SQLException
	 */
	public Result execute() throws SQLException {
		return database.execute(query);
	}

	/**
	 * Executes a query with the given list of parameters.
	 *
	 * @param parameters the parameters of the query
	 * @throws SQLException
	 */
	public Result execute(Object... parameters) throws SQLException {
		return database.execute(query, parameters);
	}
}
