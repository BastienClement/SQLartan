package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.util.DataConverter;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * A results row.
 */
public class Row implements Structure<ResultColumn> {
	/**
	 * TODO
	 */
	private Result.QueryResult res;

	/**
	 * TODO
	 */
	private RowData data;

	/**
	 * TODO
	 */
	private int currentColumn = 1;

	/**
	 * TODO
	 *
	 * @param res
	 * @param rs
	 */
	Row(Result.QueryResult res, ResultSet rs) {
		this.res = res;
		this.data = new RowData(res, rs);
	}

	/**
	 * TODO
	 *
	 * @param res
	 * @param rd
	 */
	private Row(Result.QueryResult res, RowData rd) {
		this.res = res;
		this.data = rd;
	}

	/**
	 * TODO
	 */
	public void reset() {
		currentColumn = 1;
	}

	/**
	 * TODO
	 *
	 * @param columns
	 * @return
	 */
	private Function<Table, ImmutableList<ResultColumn>> updatePartialFilter(ImmutableList<ResultColumn> columns) {
		return table -> {
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
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	private Optional<ImmutableList<ResultColumn>> updateKeys() {
		return res.uniqueColumns()
		          .map(list -> list.filter(col -> getObject(col.index()) != null))
		          .flatMap(list -> list.findFirst()
		                           .map(GeneratedColumn::sourceTable)
		                           .map(ot -> ot.orElseThrow(IllegalStateException::new))
		                           .map(updatePartialFilter(list))
		          );
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean editable() {
		return updateKeys().map(l -> !l.isEmpty()).orElse(false);
	}

	/**
	 * TODO
	 *
	 * @param column
	 * @param value
	 * @return
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public Result update(ResultColumn column, Object value) {
		if (!editable()) throw new UnsupportedOperationException("Column is not editable");
		ImmutableList<ResultColumn> keys = updateKeys().get();

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
	 * TODO
	 *
	 * @param index
	 * @param value
	 * @return
	 */
	public Optional<Result> update(int index, Object value) {
		return column(index).map(c -> update(c, value));
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @param value
	 * @return
	 */
	public Optional<Result> update(String label, Object value) {
		return column(label).map(c -> update(c, value));
	}

	/**
	 * Returns a new view of this row with an independent currentColumn counter.
	 * Both Rows are backed by the same data object.
	 *
	 * @return
	 */
	public Row view() {
		return new Row(res, data);
	}

	/**
	 * Returns the number of columns of this row.
	 *
	 * @return
	 */
	public int size() {
		return data.values.length;
	}

	/**
	 * TODO
	 *
	 * @return
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

	//###################################################################
	// Column proxy
	//###################################################################

	/**
	 * TODO
	 *
	 * @return
	 */
	@Override
	public ImmutableList<ResultColumn> columns() {
		return res.columns();
	}

	/**
	 * TODO
	 *
	 * @param name the name of the column
	 * @return
	 */
	@Override
	public Optional<ResultColumn> column(String name) {
		return res.column(name);
	}

	/**
	 * TODO
	 *
	 * @param idx the index of the column
	 * @return
	 */
	@Override
	public Optional<ResultColumn> column(int idx) {
		return res.column(idx);
	}

	//###################################################################
	// Label access
	//###################################################################

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public Object getObject(String label) {
		return data.valuesIndex.get(label);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @param tClass
	 * @param <T>
	 * @return
	 */
	public <T> T getObject(String label, Class<T> tClass) {
		return DataConverter.convert(getObject(label), tClass);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public Integer getInt(String label) {
		return getObject(label, Integer.class);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public Long getLong(String label) {
		return getObject(label, Long.class);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public Double getDouble(String label) {
		return getObject(label, Double.class);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public String getString(String label) {
		return getObject(label, String.class);
	}

	/**
	 * TODO
	 *
	 * @param label
	 * @return
	 */
	public byte[] getBytes(String label) {
		throw new UnsupportedOperationException();
	}

	//###################################################################
	// Index access
	//###################################################################

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public Object getObject(int index) {
		return (index > 0 && index <= data.values.length) ? data.values[index - 1] : null;
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @param tClass
	 * @param <T>
	 * @return
	 */
	public <T> T getObject(int index, Class<T> tClass) {
		return DataConverter.convert(getObject(index), tClass);
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public Integer getInt(int index) {
		return getObject(index, Integer.class);
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public Long getLong(int index) {
		return getObject(index, Long.class);
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public Double getDouble(int index) {
		return getObject(index, Double.class);
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public String getString(int index) {
		return getObject(index, String.class);
	}

	/**
	 * TODO
	 *
	 * @param index
	 * @return
	 */
	public byte[] getBytes(int index) {
		throw new UnsupportedOperationException();
	}

	//###################################################################
	// Sequential access
	//###################################################################

	/**
	 * TODO
	 *
	 * @return
	 */
	public Object getObject() {
		return getObject(currentColumn++);
	}

	/**
	 * TODO
	 *
	 * @param tClass
	 * @param <T>
	 * @return
	 */
	public <T> T getObject(Class<T> tClass) {
		return getObject(currentColumn++, tClass);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Integer getInt() {
		return getInt(currentColumn++);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Long getLong() {
		return getLong(currentColumn++);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Double getDouble() {
		return getDouble(currentColumn++);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public String getString() {
		return getString(currentColumn++);
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public byte[] getBytes() {
		return getBytes(currentColumn++);
	}

	//###################################################################
	// Data storage
	//###################################################################

	/**
	 * TODO
	 */
	private class RowData {
		/**
		 * TODO
		 */
		private Object[] values;

		/**
		 * TODO
		 */
		private TreeMap<String, Object> valuesIndex = new TreeMap<>();

		/**
		 * TODO
		 *
		 * @param res
		 * @param rs
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
