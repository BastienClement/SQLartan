package sqlartan.core;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * A insertion row allowing to insert data in a table.
 * <p>
 * Insertion rows does not have a fixed number of columns. Instead, they are
 * created empty and expanded on each call to .set(). It is the responsibility
 * of the user to ensure that the final have the correct shape for insertion.
 * <p>
 * Instances of this class are created by calling .insert() on a Table object.
 */
public class InsertRow {
	/**
	 * The table in which the data will be inserted
	 */
	private Table table;

	/**
	 * The list of objects to insert in the table
	 */
	private List<Object> data = new ArrayList<>();

	/**
	 * @param table the table in which the data will be inserted
	 */
	InsertRow(Table table) {
		this.table = table;
	}

	/**
	 * Defines the value of a new column in the row.
	 *
	 * @param value the value to insert in the column
	 * @return this object
	 */
	public InsertRow set(Object value) {
		data.add(value);
		return this;
	}


	/**
	 * Defines the value of a column in the row.
	 *
	 * @param index the index of the column, 1-based
	 * @param value the value to insert in the column
	 * @return this object
	 */
	public InsertRow set(int index, Object value) {
		if (--index < 0) throw new IndexOutOfBoundsException();
		while (data.size() < index) data.add(null);
		data.add(index, value);
		return this;
	}

	/**
	 * Defines the values of multiple new columns in the row.
	 *
	 * @param values the values to insert in the column
	 * @return this object
	 */
	public InsertRow set(Object... values) {
		for (Object value : values) set(value);
		return this;
	}

	/**
	 * Clear the insert row, removing every columns and associated data.
	 */
	public void clear() {
		data.clear();
	}

	/**
	 * Inserts defined data in a new row in the associated table.
	 *
	 * @return this object
	 *
	 * @throws SQLException if an error occurs while inserting data
	 */
	public Result execute() throws SQLException {
		int cardinality = data.size();

		String phs = String.join(", ", (Iterable<String>) Stream.generate(() -> "?").limit(cardinality)::iterator);
		PreparedQuery query = table.database.assemble("INSERT INTO ", table.fullName(), " VALUES (" + phs + ")")
		                                    .prepare();

		for (int i = 0; i < cardinality; i++) {
			query.set(i + 1, data.get(i));
		}

		return query.execute();
	}
}
