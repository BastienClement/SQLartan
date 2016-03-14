package sqlartan;

import java.sql.*;

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello, world!");
	}

	/**
	 * Returns the current SQLite version.
	 *
	 * @return
	 * @throws SQLException
	 */
	public static String sqliteVersion() throws SQLException {
		try (Connection connection = DriverManager.getConnection("jdbc:sqlite::memory:");
		     Statement statement = connection.createStatement();
		     ResultSet rs = statement.executeQuery("select sqlite_version();")) {
			return rs.getString(1);
		}
	}
}
