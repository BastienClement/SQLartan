package sqlartan.core;

import sqlartan.core.util.RuntimeSQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedQuery {
	private PreparedStatement stmt;

	PreparedQuery(Connection connection, String sql) throws SQLException {
		stmt = connection.prepareStatement(sql);
	}

	public PreparedQuery set(int idx, int value) {
		try {
			stmt.setInt(idx, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	public PreparedQuery set(int idx, long value) {
		try {
			stmt.setLong(idx, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	public PreparedQuery set(int idx, double value) {
		try {
			stmt.setDouble(idx, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	public PreparedQuery set(int idx, String value) {
		try {
			stmt.setString(idx, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	public PreparedQuery set(int idx, Object value) {
		try {
			stmt.setObject(idx, value);
			return this;
		} catch (SQLException e) {
			throw new RuntimeSQLException(e);
		}
	}

	public Result execute() throws SQLException {
		return Result.fromPreparedStatement(stmt);
	}
}
