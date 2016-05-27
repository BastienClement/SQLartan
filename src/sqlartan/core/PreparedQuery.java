package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Defines a prepared query
 */
public class PreparedQuery {
	/**
	 * The database on which the query will be executed
	 */
	private Database database;

	/**
	 * Contains the SQL of the query
	 */
	private String sql;

	/**
	 * Prepared statement for executing the query
	 */
	private PreparedStatement stmt;

	/**
	 * Constructs a prepared query with the given database, connection and SQL
	 *
	 * @param database
	 * @param connection
	 * @param sql
	 * @throws SQLException
	 */
	PreparedQuery(Database database, Connection connection, String sql) throws SQLException {
		this.database = database;
		this.sql = sql;
		stmt = connection.prepareStatement(sql);
	}

	/**
	 * TODO
	 *
	 * @param idx
	 * @param value
	 * @return
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
	 * TODO
	 *
	 * @param idx
	 * @param value
	 * @return
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
	 * TODO
	 *
	 * @param idx
	 * @param value
	 * @return
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
	 * TODO
	 *
	 * @param idx
	 * @param value
	 * @return
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
	 * TODO
	 *
	 * @param idx
	 * @param value
	 * @return
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
	 * TODO
	 *
	 * @return
	 * @throws SQLException
	 */
	public Result execute() throws SQLException {
		return database.notifyListeners(sql, Result.fromPreparedStatement(database, stmt, sql));
	}
}
