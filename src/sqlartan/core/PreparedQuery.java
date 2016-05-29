package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * A prepared query with data placeholder.
 */
public class PreparedQuery {
	/**
	 * The database on which the query will be executed
	 */
	private Database database;

	/**
	 * The source SQL query
	 */
	private String sql;

	/**
	 * The JDBC prepared statement used
	 */
	private PreparedStatement stmt;

	/**
	 * @param database   the database on which the query will be executed
	 * @param connection the JDBC connection to use
	 * @param sql        the source SQL query
	 * @throws SQLException if the query is invalid
	 */
	PreparedQuery(Database database, Connection connection, String sql) throws SQLException {
		this.database = database;
		this.sql = sql;
		stmt = connection.prepareStatement(sql);
	}

	/**
	 * Defines the integer value of a placeholder.
	 *
	 * @param idx   the placeholder index, 1-based
	 * @param value the value to use for the placeholder
	 * @return this object
	 */
	public PreparedQuery set(int idx, int value) {
		try {
			stmt.setInt(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Defines the long integer value of a placeholder.
	 *
	 * @param idx   the placeholder index, 1-based
	 * @param value the value to use for the placeholder
	 * @return this object
	 */
	public PreparedQuery set(int idx, long value) {
		try {
			stmt.setLong(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Defines the double value of a placeholder.
	 *
	 * @param idx   the placeholder index, 1-based
	 * @param value the value to use for the placeholder
	 * @return this object
	 */
	public PreparedQuery set(int idx, double value) {
		try {
			stmt.setDouble(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Defines the string value of a placeholder.
	 *
	 * @param idx   the placeholder index, 1-based
	 * @param value the value to use for the placeholder
	 * @return this object
	 */
	public PreparedQuery set(int idx, String value) {
		try {
			stmt.setString(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Defines the generic object value of a placeholder.
	 *
	 * @param idx   the placeholder index, 1-based
	 * @param value the value to use for the placeholder
	 * @return this object
	 */
	public PreparedQuery set(int idx, Object value) {
		try {
			stmt.setObject(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Executes the prepared query.
	 *
	 * @return the result set
	 *
	 * @throws SQLException if the query is invalid
	 */
	public Result execute() throws SQLException {
		return database.notifyListeners(Result.fromPreparedStatement(database, stmt, sql));
	}
}
