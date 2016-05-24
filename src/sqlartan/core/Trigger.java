package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class Trigger
{
	private final Table table;
	private String name;
	private String content;
	private List<TableColumn> columns = new LinkedList<>();

	public Trigger(Table table, String name, String content) {
		this.table = table;
		this.name = name;
		this.content = content;
	}

	public void rename(String newName) {
		try {
			// Replace the name in the trigger creation sql
			// Execute the new sql, add the new trigger
			table.database().execute(content.replaceAll(name, newName));

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
			table.database().assemble("DROP TRIGGER ", table.database().name(), ".", name).execute();
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
