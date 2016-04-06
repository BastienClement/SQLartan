package sqlartan.core;

import sqlartan.core.exception.RuntimeSQLException;
import sqlartan.core.util.DataConverter;
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

	private Object[] values;
	private TreeMap<String, Object> valuesIndex = new TreeMap<>();

	private int currentColumn = 1;

	Row(Result res, ResultSet rs) {
		List<GeneratedColumn> columns = res.columns();
		values = new Object[columns.size()];

		columns.forEach(col -> {
			try {
				Object value = rs.getObject(currentColumn);
				values[currentColumn - 1] = value;
				valuesIndex.put(col.name(), value);
				currentColumn++;
			} catch (SQLException e) {
				throw new RuntimeSQLException(e);
			}
		});

		this.res = res;
		reset();
	}

	public void reset() {
		currentColumn = 1;
	}

	//###################################################################
	// QueryStructure proxy
	//###################################################################

	@Override
	public List<PersistentStructure<GeneratedColumn>> sources() {
		return res.sources();
	}

	@Override
	public List<GeneratedColumn> columns() {
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
		return valuesIndex.get(label);
	}

	public <T> T getObject(String label, Class<T> tClass) {
		return DataConverter.convert(getObject(label), tClass);
	}

	public int getInt(String label) {
		return getObject(label, Integer.class);
	}

	public long getLong(String label) {
		return getObject(label, Long.class);
	}

	public double getDouble(String label) {
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
		return (index > 0 && index <= values.length) ? values[index - 1] : null;
	}

	public <T> T getObject(int index, Class<T> tClass) {
		return DataConverter.convert(getObject(index), tClass);
	}

	public int getInt(int index) {
		return getObject(index, Integer.class);
	}

	public long getLong(int index) {
		return getObject(index, Long.class);
	}

	public double getDouble(int index) {
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

	public int getInt() {
		return getInt(currentColumn++);
	}

	public long getLong() {
		return getLong(currentColumn++);
	}

	public double getDouble() {
		return getDouble(currentColumn++);
	}

	public String getString() {
		return getString(currentColumn++);
	}

	public byte[] getBytes() {
		return getBytes(currentColumn++);
	}
}
