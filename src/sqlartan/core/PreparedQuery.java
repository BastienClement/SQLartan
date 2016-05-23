package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedQuery {
	private Database database;
	private String sql;
	private PreparedStatement stmt;

	PreparedQuery(Database database, Connection connection, String sql) throws SQLException {
		this.database = database;
		this.sql = sql;
		stmt = connection.prepareStatement(sql);
	}

	public PreparedQuery set(int idx, int value) {
		try {
			stmt.setInt(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public PreparedQuery set(int idx, long value) {
		try {
			stmt.setLong(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public PreparedQuery set(int idx, double value) {
		try {
			stmt.setDouble(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public PreparedQuery set(int idx, String value) {
		try {
			stmt.setString(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public PreparedQuery set(int idx, Object value) {
		try {
			stmt.setObject(idx, value);
			return this;
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public Result execute() throws SQLException {
		return Result.fromPreparedStatement(database, stmt, sql);
	}
}
