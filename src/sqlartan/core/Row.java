package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.DataConverter;
import sqlartan.core.util.RuntimeSQLException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

/**
 * A results row.
 */
public class Row implements QueryStructure<GeneratedColumn> {
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

	public void reset() {
		currentColumn = 1;
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

	@Override
	public String toString() {
		List<GeneratedColumn> columns = res.columns();
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
	// QueryStructure proxy
	//###################################################################

	@Override
	public IterableStream<PersistentStructure<? extends Column>> sources() {
		return res.sources();
	}

	@Override
	public ImmutableList<GeneratedColumn> columns() {
		return res.columns();
	}

	@Override
	public Optional<GeneratedColumn> column(String name) {
		return res.column(name);
	}

	@Override
	public Optional<GeneratedColumn> column(int idx) {
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
			List<GeneratedColumn> columns = res.columns();
			values = new Object[columns.size()];

			int currentColumn = 1;
			for (GeneratedColumn col : columns) {
				try {
					Object value = rs.getObject(currentColumn);
					values[currentColumn - 1] = value;
					valuesIndex.put(col.name(), value);
					currentColumn++;
				} catch (SQLException e) {
					throw new RuntimeSQLException(e);
				}
			}
		}
	}
}
