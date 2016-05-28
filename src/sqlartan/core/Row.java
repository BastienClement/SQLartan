package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.util.DataConverter;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.Lazy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import static sqlartan.util.Lazy.lazy;

/**
 * A results set row.
 * <p>
 * The row data is read once from the JDBC result set and are kept for the
 * lifespan of the instance.
 * <p>
 * Rows have an internal column cursor allowing to consume data in a stream
 * like fashion. Each call of a reader method will return the next column
 * from the row. Care must be taken when using the same instance of the row
 * in multiple places.
 * <p>
 * A new view of the row can be created to create a new row instance with
 * an independent cursor. The actual row data will be shared between the
 * original row and its view.
 */
public class Row implements Structure<ResultColumn> {
	/**
	 * The parent result set
	 */
	private Result.QueryResult res;

	/**
	 * The data in this row
	 */
	private RowData data;

	/**
	 * The current column cursor
	 */
	private int currentColumn = 1;

	/**
	 * @param res the parent result set
	 * @param rs  the JDBC result set from which data must be read
	 */
	Row(Result.QueryResult res, ResultSet rs) {
		this.res = res;
		this.data = new RowData(res, rs);
	}

	/**
	 * View constructor.
	 *
	 * @param res the parent result set
	 * @param rd  the row data object to use
	 */
	private Row(Result.QueryResult res, RowData rd) {
		this.res = res;
		this.data = rd;
	}

	/**
	 * Resets the internal column cursor.
	 */
	public void reset() {
		currentColumn = 1;
	}

	/**
	 * A list of unique columns in this row that can be used as keys for
	 * updating it. Each columns in the resulting list can be used to uniquely
	 * identify the corresponding row in the source table.
	 * <p>
	 * Such a list can only be computed if the list of unique columns from the
	 * result set can be computed, that is, if the source query in a simple
	 * SELECT query using only a single table and column references.
	 * <p>
	 * In addition, columns that are NULL in this row are excluded from the
	 * list as well as columns for which no corresponding PRIMARY KEY or
	 * UNIQUE index is fully contained in the result set.
	 * <p>
	 * For example, if only one column of a two-columns PRIMARY KEY index
	 * is selected in the result set, the resulting rows are not editable
	 * despite the selected column being considered "unique".
	 */
	private Lazy<Optional<ImmutableList<ResultColumn>>> updateKeys = lazy(() -> {
		Function<ImmutableList<ResultColumn>, Function<Table, ImmutableList<ResultColumn>>> updatePartialFilter =
			columns -> table -> {
				ImmutableList<Index> indices = table.indices();
				return columns.filter(
					col -> (col.type().equals("INTEGER") && col.sourceColumn().orElseThrow(IllegalStateException::new).primaryKey()) || indices.exists(
						index -> index.columns().allMatch(
							name -> columns.exists(
								c -> c.sourceColumn().orElseThrow(IllegalStateException::new).name().equals(name)
							)
						)
					)
				);
			};

		return res.uniqueColumns()
		          .map(list -> list.filter(col -> getObject(col.index()) != null))
		          .flatMap(list -> list.findFirst()
		                               .map(GeneratedColumn::sourceTable)
		                               .map(ot -> ot.orElseThrow(IllegalStateException::new))
		                               .map(updatePartialFilter.apply(list))
		          );
	});

	/**
	 * Checks if this row is editable.
	 * <p>
	 * A row is editable if at least one unique column is present in the
	 * result set and is not NULL in the current row.
	 *
	 * @return true if this row can be edited
	 */
	public boolean editable() {
		return updateKeys.get().map(l -> !l.isEmpty()).orElse(false);
	}

	/**
	 * Updates the value of a column of the row.
	 *
	 * @param column the column to update
	 * @param value  the new value of the column for this row
	 * @return the result set of the update query
	 *
	 * @throws UnsupportedOperationException if the row is not editable
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public Result update(ResultColumn column, Object value) {
		if (!editable()) throw new UnsupportedOperationException("Row is not editable");
		ImmutableList<ResultColumn> keys = updateKeys.get().get();

		Table table = column.sourceTable().get();
		TableColumn target = column.sourceColumn().get();

		ImmutableList<String> refs = keys.map(c -> c.sourceColumn().get().name());
		ImmutableList<Object> data = keys.map(c -> getObject(c.index()));

		String phs = String.join(" AND ", refs.map(n -> "[" + n + "]" + " = ?"));

		try {
			PreparedQuery pq = res.database()
			                      .assemble("UPDATE ", table.fullName(), " SET ", target.name(), " = ? WHERE " + phs)
			                      .prepare();
			pq.set(1, value);
			for (int i = 0, j = 2; i < data.size(); i++, j++) { pq.set(j, data.get(i)); }
			return pq.execute();
		} catch (SQLException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * Update the value of the column with the given index.
	 *
	 * @param index the column index, 0-based
	 * @param value the new value of the column for this row
	 * @return the result set of the update statement
	 */
	public Result update(int index, Object value) {
		return update(column(index).orElseThrow(IndexOutOfBoundsException::new), value);
	}

	/**
	 * Update the value of the column with the given label.
	 *
	 * @param label the column label
	 * @param value the new value of the column for this row
	 * @return the result set of the update statement
	 */
	public Result update(String label, Object value) {
		return update(column(label).orElseThrow(NoSuchElementException::new), value);
	}

	/**
	 * Returns a new view of this row with an independent currentColumn counter.
	 * Both Rows are backed by the same data object.
	 *
	 * @return a new view of this row
	 */
	public Row view() {
		return new Row(res, data);
	}

	/**
	 * Returns the number of columns of this row.
	 *
	 * @return the number of column of this row
	 */
	public int size() {
		return data.values.length;
	}

	/**
	 * Returns a string representation of the row.
	 *
	 * @return a string representation of the row
	 */
	@Override
	public String toString() {
		List<ResultColumn> columns = res.columns();
		StringBuilder sb = new StringBuilder();

		sb.append("Row(");
		for (int i = 0; i < data.values.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(columns.get(i).name());
			sb.append(": ");
			sb.append(data.values[i].toString());
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableList<ResultColumn> columns() {
		return res.columns();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ResultColumn> column(String name) {
		return res.column(name);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ResultColumn> column(int idx) {
		return res.column(idx);
	}

	//###################################################################
	// Label access
	//###################################################################

	/**
	 * Returns the object value of the column with the given label.
	 * The returned object will have the same class as the actual type of data
	 * stored in the database.
	 *
	 * @param label the column label
	 * @return the value of the column
	 */
	public Object getObject(String label) {
		return data.valuesIndex.get(label);
	}

	/**
	 * Returns the value of the column with the given label.
	 * The actual value type will be converted to the requested type.
	 *
	 * @param label  the column label
	 * @param tClass the requested value type
	 * @param <T>    the type of the returned value
	 * @return the value of the column
	 *
	 * @throws UnsupportedOperationException if the value cannot be converted
	 *                                       to the requested type.
	 */
	public <T> T getObject(String label, Class<T> tClass) {
		return DataConverter.convert(getObject(label), tClass);
	}

	/**
	 * Returns the integer value of the column with the given label.
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param label the column label
	 * @return the value of the column
	 */
	public Integer getInt(String label) {
		return getObject(label, Integer.class);
	}

	/**
	 * Returns the long value of the column with the given label.
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param label the column label
	 * @return the value of the column
	 */
	public Long getLong(String label) {
		return getObject(label, Long.class);
	}

	/**
	 * Returns the double value of the column with the given label.
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param label the column label
	 * @return the value of the column
	 */
	public Double getDouble(String label) {
		return getObject(label, Double.class);
	}

	/**
	 * Returns the string value of the column with the given label.
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param label the column label
	 * @return the value of the column
	 */
	public String getString(String label) {
		return getObject(label, String.class);
	}

	//###################################################################
	// Index access
	//###################################################################

	/**
	 * Returns the object value of the column with the given index.
	 * <p>
	 * The returned object will have the same class as the actual type of data
	 * stored in the database.
	 *
	 * @param index the column index
	 * @return the value of the column
	 */
	public Object getObject(int index) {
		return (index > 0 && index <= data.values.length) ? data.values[index - 1] : null;
	}

	/**
	 * Returns the value of the column with the given index.
	 * <p>
	 * The actual value type will be converted to the requested type.
	 *
	 * @param index  the column index
	 * @param tClass the requested value type
	 * @param <T>    the type of the returned value
	 * @return the value of the column
	 *
	 * @throws UnsupportedOperationException if the value cannot be converted
	 *                                       to the requested type.
	 */
	public <T> T getObject(int index, Class<T> tClass) {
		return DataConverter.convert(getObject(index), tClass);
	}

	/**
	 * Returns the integer value of the column with the given index.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param index the column index
	 * @return the value of the column
	 */
	public Integer getInt(int index) {
		return getObject(index, Integer.class);
	}

	/**
	 * Returns the long value of the column with the given index.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param index the column index
	 * @return the value of the column
	 */
	public Long getLong(int index) {
		return getObject(index, Long.class);
	}

	/**
	 * Returns the double value of the column with the given index.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param index the column index
	 * @return the value of the column
	 */
	public Double getDouble(int index) {
		return getObject(index, Double.class);
	}

	/**
	 * Returns the string value of the column with the given index.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 *
	 * @param index the column index
	 * @return the value of the column
	 */
	public String getString(int index) {
		return getObject(index, String.class);
	}

	//###################################################################
	// Sequential access
	//###################################################################

	/**
	 * Returns the object value of the next column.
	 * <p>
	 * The returned object will have the same class as the actual type of data
	 * stored in the database.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @return the value of the next column
	 */
	public Object getObject() {
		return getObject(currentColumn++);
	}

	/**
	 * Returns the value of the column with the given index.
	 * <p>
	 * The actual value type will be converted to the requested type.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @param tClass the requested value type
	 * @param <T>    the type of the returned value
	 * @return the value of the next column
	 *
	 * @throws UnsupportedOperationException if the value cannot be converted
	 *                                       to the requested type.
	 */
	public <T> T getObject(Class<T> tClass) {
		return getObject(currentColumn++, tClass);
	}

	/**
	 * Returns the integer value of the next column.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @return the value of the next column
	 */
	public Integer getInt() {
		return getInt(currentColumn++);
	}

	/**
	 * Returns the long value of the next column.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @return the value of the next column
	 */
	public Long getLong() {
		return getLong(currentColumn++);
	}

	/**
	 * Returns the double value of the next column.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @return the value of the next column
	 */
	public Double getDouble() {
		return getDouble(currentColumn++);
	}

	/**
	 * Returns the string value of the next column.
	 * <p>
	 * If the requested type does not match the one stored in the database,
	 * the value will be automatically converted to the requested type.
	 * <p>
	 * This method will move the internal cursor one column forward.
	 *
	 * @return the value of the next column
	 */
	public String getString() {
		return getString(currentColumn++);
	}

	//###################################################################
	// Data storage
	//###################################################################

	/**
	 * Internal row data storage.
	 * <p>
	 * A single instances of this class is shared between all views of the
	 * row, thus implementing the flyweight pattern.
	 */
	private class RowData {
		/**
		 * The values of each columns in this row
		 */
		private Object[] values;

		/**
		 * Index of values by column name
		 */
		private TreeMap<String, Object> valuesIndex = new TreeMap<>();

		/**
		 * @param res the result set
		 * @param rs  the JDBC result set to read
		 */
		private RowData(Result.QueryResult res, ResultSet rs) {
			List<ResultColumn> columns = res.columns();
			values = new Object[columns.size()];

			int currentColumn = 1;
			for (GeneratedColumn col : columns) {
				try {
					Object value = rs.getObject(currentColumn);
					values[currentColumn - 1] = value;
					valuesIndex.put(col.name(), value);
					currentColumn++;
				} catch (SQLException e) {
					throw new UncheckedSQLException(e);
				}
			}
		}
	}
}
