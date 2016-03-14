package sqlartan;

import java.sql.*;

public class Main {
	public static void main(String[] args) {
		System.out.println("Hello, world!");
	}

	public static String sqliteVersion() throws SQLException {
		Connection c = null;
		try {
			c = DriverManager.getConnection("jdbc:sqlite::memory:");
			ResultSet rs = c.createStatement().executeQuery("select sqlite_version();");
			rs.next();
			return rs.getString(1);
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					// close failed ?!
				}
			}
		}

	}
}
