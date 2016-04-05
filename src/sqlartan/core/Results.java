package sqlartan.core;

import sun.plugin.dom.exception.InvalidStateException;
import java.sql.*;
import java.util.*;
import java.util.stream.Stream;

public class Results implements QueryStructure<GeneratedColumn>, Iterable<Results.Row>, AutoCloseable {
	/** Type of the Results object */
	private enum Type {
		Update, Query
	}

	private Results.Type type;

	private int updateCount = 0;

	private ResultSet resultSet;
	private boolean storedResults = false;
	private boolean consumed = false;
	private ArrayList<Row> rows;
	private int currentRow = -1;

	private ArrayList<GeneratedColumn> columns;
	private HashMap<String, GeneratedColumn> columnsIndex;

	Results(Connection connection, String query, boolean storedResults) throws SQLException {
		try (Statement statement = connection.createStatement()) {
			if (statement.execute(query)) {
				type = Type.Query;
				resultSet = statement.getResultSet();
				readMetadata();
				this.storedResults = storedResults;
			} else {
				type = Type.Update;
				updateCount = statement.getUpdateCount();
			}
		}
	}

	/**
	 * Reads metadata information from the ResultSet.
	 *
	 * @throws SQLException
	 */
	private void readMetadata() throws SQLException {
		ResultSetMetaData meta = resultSet.getMetaData();
		int count = meta.getColumnCount();

		columns = new ArrayList<>(count);
		columnsIndex = new HashMap<>();

		for (int i = 1; i <= count; i++) {
			String name = meta.getColumnName(i);
			String table = meta.getTableName(i);
			String type = meta.getColumnTypeName(i);

			GeneratedColumn col = new GeneratedColumn(new GeneratedColumn.Properties() {
				public String name() { return name; }
				public String type() {
					return type;
				}
				public String sourceTable() { return table; }
				public String sourceExpr() { throw new UnsupportedOperationException("Not implemented"); }
			});

			columns.add(col);
			columnsIndex.put(name, col);
		}
	}

	/**
	 * Returns if this object is a result of an UPDATE or DELETE query.
	 * Also returns true if the query was a DDL one, but updateCount() will always return 0 in this case.
	 *
	 * @return
	 */
	public boolean isUpdateResults() {
		return type == Type.Update;
	}

	/**
	 * Returns if this object is a list of results from a SELECT query.
	 *
	 * @return
	 */
	public boolean isQueryResults() {
		return type == Type.Update;
	}

	public boolean isConsumed() {
		return consumed;
	}

	/**
	 * Returns the number of rows updated by the query.
	 *
	 * @return
	 */
	public int updateCount() {
		return updateCount;
	}

	/**
	 * Returns the sources used to generate these results.
	 *
	 * @return
	 */
	@Override
	public List<PersistentStructure<GeneratedColumn>> sources() {
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Returns an unmodifiable list of the columns composing these results.
	 * Only application if this Results is a Query result.
	 *
	 * @return
	 */
	@Override
	public List<GeneratedColumn> columns() {
		return type == Type.Query ? Collections.unmodifiableList(columns) : Collections.EMPTY_LIST;
	}

	/**
	 * Returns the column with the given name
	 * Only application if this Results is a Query result.
	 *
	 * @param name
	 * @return
	 */
	@Override
	public Optional<GeneratedColumn> column(String name) {
		return type == Type.Query ? Optional.ofNullable(columnsIndex.get(name)) : Optional.empty();
	}

	/**
	 * Returns the column at a given index.
	 * Only application if this Results is a Query result.
	 *
	 * @param idx
	 * @return
	 */
	@Override
	public Optional<GeneratedColumn> column(int idx) {
		if (idx < 0 || idx >= columns.size()) return Optional.empty();
		return type == Type.Query ? Optional.of(columns.get(idx)) : Optional.empty();
	}

	private void checkConsumed() {
		if (storedResults) return;
		if (consumed) throw new InvalidStateException("Stream has already been consumed");
		consumed = true;
	}

	public void enableStorage() {
		this.checkConsumed();
		storedResults = true;
		rows = new ArrayList<>();
	}

	@Override
	public Iterator<Row> iterator() {
		this.checkConsumed();
		return new ResultsIterator();
	}

	public Stream<Row> stream() {
		this.checkConsumed();
		return Stream.empty();
	}

	/**
	 * Closes the Results. Freeing the underlying ResultSet if applicable.
	 *
	 * @throws SQLException
	 */
	@Override
	public void close() throws SQLException {
		if (resultSet != null) {
			resultSet.close();
			resultSet = null;
		}
	}

	class Row {
		public Row() {
			// consume data here
		}
	}

	class ResultsIterator implements Iterator<Row> {
		private int currentRow = 0;

		@Override
		public boolean hasNext() {
			return false;
		}

		@Override
		public Row next() {
			return null;
		}

		@Override
		public void remove() {

		}
	}
}
