package sqlartan.core;

import java.sql.SQLException;

/**
 * An assembled query that can be executed on the database.
 * Result of the assemble() method from Database.
 */
public class AssembledQuery {
	/**
	 * The database on which the query will be executed
	 */
	private final Database database;

	/**
	 * The source SQL query
	 */
	private final String query;

	/**
	 * @param database the database on which the query will be executed
	 * @param query    the source SQL query
	 */
	AssembledQuery(Database database, String query) {
		this.database = database;
		this.query = query;
	}

	/**
	 * Returns the assembled SQL query.
	 *
	 * @return the assembled SQL query
	 */
	public String query() {
		return query;
	}

	/**
	 * Transforms this assembled query to a prepared query.
	 *
	 * @return the prepared query
	 *
	 * @throws SQLException if the SQL query is invalid
	 */
	public PreparedQuery prepare() throws SQLException {
		return database.prepare(query);
	}

	/**
	 * Executes the query.
	 *
	 * @return the result set
	 *
	 * @throws SQLException if the SQL query is invalid
	 */
	public Result execute() throws SQLException {
		return database.execute(query);
	}

	/**
	 * Executes a query with the given list of parameters.
	 *
	 * @param parameters the parameters of the query
	 * @return the result set
	 *
	 * @throws SQLException if the SQL query is invalid
	 */
	public Result execute(Object... parameters) throws SQLException {
		return database.execute(query, parameters);
	}
}
