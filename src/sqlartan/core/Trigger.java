package sqlartan.core;

import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;

/**
 * Defines a trigger of a database
 */
public class Trigger
{
	/**
	 * The table containing the trigger
	 */
	private final Table table;

	/**
	 * The name of the trigger
	 */
	private String name;

	/**
	 * sql definition
	 */
	private String content;

	/**
	 * Constructs a new trigger on the given table and with the given name and content.
	 *
	 * @param table
	 * @param name
	 * @param content
	 */
	public Trigger(Table table, String name, String content) {
		this.table = table;
		this.name = name;
		this.content = content;
	}

	/**
	 * Rename the trigger.
	 *
	 * @param newName
	 */
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

	/**
	 * Drop the trigger.
	 */
	public void drop() {
		try {
			table.database().assemble("DROP TRIGGER ", table.database().name(), ".", name).execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Get the name of the trigger
	 *
	 * @return the name
	 */
	public String getName(){
		return name;
	}

	/**
	 * Get the content of the trigger
	 *
	 * @return the content
	 */
	public String getContent(){
		return content;
	}
}
