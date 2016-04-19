package sqlartan.core;

import java.io.File;
import java.sql.SQLException;

public class AttachedDatabase extends Database {
	private Database main;

	AttachedDatabase(File path, String name, Database main) throws SQLException {
		super(path, name);
		this.main = main;
	}

	public Database main() {
		return main;
	}
}
