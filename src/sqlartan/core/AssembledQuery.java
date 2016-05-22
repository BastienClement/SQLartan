package sqlartan.core;

import java.sql.SQLException;

/**
 * TODO
 */
public class AssembledQuery {
	private final Database database;
	private final String query;

	AssembledQuery(Database database, String query) {
		this.database = database;
		this.query = query;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String query() {
		return query;
	}

	/**
	 * TODO
	 *
	 * @return
	 * @throws SQLException
	 */
	public PreparedQuery prepare() throws SQLException {
		return database.prepare(query);
	}

	/**
	 * TODO
	 *
	 * @throws SQLException
	 */
	public Result execute() throws SQLException {
		return database.execute(query);
	}

	/**
	 * TODO
	 *
	 * @param parameters
	 * @throws SQLException
	 */
	public Result execute(Object... parameters) throws SQLException {
		return database.execute(query, parameters);
	}
}
