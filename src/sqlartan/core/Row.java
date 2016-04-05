package sqlartan.core;

import sqlartan.core.exception.InvalidDataReadException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * A results row.
 *
 * This object is dynamically linked to the state of the internal ResultSet of the Results object.
 * It should not be stored or used outside the scope of the iterator or stream where it was obtained.
 */
public class Row implements QueryStructure<GeneratedColumn> {
	private Results res;
	private ResultSet rs;
	private int currentColumn;

	Row(Results res, ResultSet rs) {
		this.res = res;
		this.rs = rs;
		reset();
	}

	public void reset() {
		currentColumn = 1;
	}

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

	@SuppressWarnings("unchecked")
	public <T> T getObject(String label) {
		try {
			return (T) rs.getObject(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public int getInt(String label) {
		try {
			return rs.getInt(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public long getLong(String label) {
		try {
			return rs.getInt(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public double getDouble(String label) {
		try {
			return rs.getDouble(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public String getString(String label) {
		try {
			return rs.getString(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public byte[] getBytes(String label) {
		try {
			return rs.getBytes(label);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	//###################################################################
	// Index access
	//###################################################################

	@SuppressWarnings("unchecked")
	public <T> T getObject(int index) {
		try {
			return (T) rs.getObject(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public int getInt(int index) {
		try {
			return rs.getInt(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public long getLong(int index) {
		try {
			return rs.getInt(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public double getDouble(int index) {
		try {
			return rs.getDouble(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public String getString(int index) {
		try {
			return rs.getString(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	public byte[] getBytes(int index) {
		try {
			return rs.getBytes(index);
		} catch (SQLException e) {
			throw new InvalidDataReadException(e);
		}
	}

	//###################################################################
	// Sequential access
	//###################################################################

	public <T> T getObject() {
		return getObject(currentColumn++);
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
