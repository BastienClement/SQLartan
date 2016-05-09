package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;

public class Trigger
{
	private final Database database;
	private String name;
	private String content;

	public Trigger(Database database, String name, String content) {
		this.database = database;
		this.name = name;
		this.content = content;
	}

	public void rename(String newName) {
		try {
			// Replace the name in the trigger creation sql
			// Execute the new sql, add the new trigger
			database.execute(content.replaceAll(name, newName));

			// Delete the old trigger
			drop();

			// Update the name and the creation sql
			name = newName;
			content = content.replaceAll(name, newName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void drop() {
		try {
			database.assemble("DROP TRIGGER ", database.name(), ".", name).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	public String getName(){
		return name;
	}

	public String getContent(){
		return content;
	}
}
