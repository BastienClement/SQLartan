package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.util.DataConverter;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * A results row.
 */
public class Row implements Structure<ResultColumn> {
	private Result res;
	private RowData data;
	private int currentColumn = 1;

	Row(Result res, ResultSet rs) {
		this.res = res;
		this.data = new RowData(res, rs);
	}

	private Row(Result res, RowData rd) {
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
	 */
	public boolean isEditable(Set<ResultColumn> updateKeys) {
		return !updateKeys.isEmpty() && updateKeys.stream().allMatch(
			col -> getObject(col.index()) != null && col.sourceColumn().isPresent());
	}

	/**
	 * TODO
	 */
	public boolean isEditable(ResultColumn column) {
		return isEditable(column.updateKeys());
	}

	/**
	 * TODO
	 * @param column
	 * @param value
	 * @return
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	public Result update(ResultColumn column, Object value) {
		Set<ResultColumn> updateKeys = column.updateKeys();
		if (!isEditable(updateKeys)) {
			throw new UnsupportedOperationException("Column is not editable");
		}

		Table table = column.sourceTable().get();
		TableColumn tcol = column.sourceColumn().get();

		List<String> refs = new ArrayList<>();
		List<Object> data = new ArrayList<>();

		updateKeys.forEach(col -> {
			refs.add(col.sourceColumn().get().name());
			data.add(getObject(col.index()));
		});

		String phs = String.join(" AND ", (Iterable<String>) refs.stream().map(n -> "[" + n + "]" + " = ?")::iterator);

		try {
			PreparedQuery pq = res.database()
			                      .assemble("UPDATE ", table.fullName(), " SET ", tcol.name(), " = ? WHERE " + phs)
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
	 * @param index
	 * @param value
	 * @return
	 */
	public Optional<Result> update(int index, Object value) {
		return column(index).map(c -> update(c, value));
	}

	/**
	 * TODO
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
	 */
	public int size() {
		return data.values.length;
	}

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
	// Structure proxy
	//###################################################################

	@Override
	public ImmutableList<ResultColumn> columns() {
		return res.columns();
	}

	@Override
	public Optional<ResultColumn> column(String name) {
		return res.column(name);
	}

	@Override
	public Optional<ResultColumn> column(int idx) {
		return res.column(idx);
	}

	//###################################################################
	// Label access
	//###################################################################

	public Object getObject(String label) {
		return data.valuesIndex.get(label);
	}

	public <T> T getObject(String label, Class<T> tClass) {
		return DataConverter.convert(getObject(label), tClass);
	}

	public Integer getInt(String label) {
		return getObject(label, Integer.class);
	}

	public Long getLong(String label) {
		return getObject(label, Long.class);
	}

	public Double getDouble(String label) {
		return getObject(label, Double.class);
	}

	public String getString(String label) {
		return getObject(label, String.class);
	}

	public byte[] getBytes(String label) {
		throw new UnsupportedOperationException();
	}

	//###################################################################
	// Index access
	//###################################################################

	public Object getObject(int index) {
		return (index > 0 && index <= data.values.length) ? data.values[index - 1] : null;
	}

	public <T> T getObject(int index, Class<T> tClass) {
		return DataConverter.convert(getObject(index), tClass);
	}

	public Integer getInt(int index) {
		return getObject(index, Integer.class);
	}

	public Long getLong(int index) {
		return getObject(index, Long.class);
	}

	public Double getDouble(int index) {
		return getObject(index, Double.class);
	}

	public String getString(int index) {
		return getObject(index, String.class);
	}

	public byte[] getBytes(int index) {
		throw new UnsupportedOperationException();
	}

	//###################################################################
	// Sequential access
	//###################################################################

	public Object getObject() {
		return getObject(currentColumn++);
	}

	public <T> T getObject(Class<T> tClass) {
		return getObject(currentColumn++, tClass);
	}

	public Integer getInt() {
		return getInt(currentColumn++);
	}

	public Long getLong() {
		return getLong(currentColumn++);
	}

	public Double getDouble() {
		return getDouble(currentColumn++);
	}

	public String getString() {
		return getString(currentColumn++);
	}

	public byte[] getBytes() {
		return getBytes(currentColumn++);
	}

	//###################################################################
	// Data storage
	//###################################################################

	private class RowData {
		private Object[] values;
		private TreeMap<String, Object> valuesIndex = new TreeMap<>();

		private RowData(Result res, ResultSet rs) {
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
