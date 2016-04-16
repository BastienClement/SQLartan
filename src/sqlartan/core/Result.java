package sqlartan.core;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Result implements QueryStructure<GeneratedColumn>, Iterable<Row>, ResultStreamOps, AutoCloseable {
	/** Type of the Results object */
	private enum Type {
		Update, Query
	}

	private Statement statement;
	private Result.Type type;

	private int updateCount = 0;

	private ArrayList<GeneratedColumn> columns;
	private HashMap<String, GeneratedColumn> columnsIndex;

	private ResultSet resultSet;
	private boolean consumed = false;
	private boolean done = false;
	private int currentRowIdx = 0;
	private Row currentRow;
	private ArrayList<Row> rows;

	Result(Connection connection, String query) throws SQLException {
		statement = connection.createStatement();
		readResult(statement.execute(query));
	}

	Result(PreparedStatement preparedStatement) throws SQLException {
		statement = preparedStatement;
		readResult(preparedStatement.execute());
	}

	/**
	 * @param executeResult
	 * @throws SQLException
	 */
	private void readResult(boolean executeResult) throws SQLException {
		if (executeResult) {
			type = Type.Query;
			resultSet = statement.getResultSet();
			readMetadata();
		} else {
			type = Type.Update;
			updateCount = statement.getUpdateCount();
			close();
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
	 * Checks if this object is a result of an UPDATE or DELETE query.
	 * Also returns true if the query was a DDL query, but updateCount() will always return 0 in this case.
	 *
	 * @return
	 */
	public boolean isUpdateResult() {
		return type == Type.Update;
	}

	/**
	 * Requires the Result object to be of type Update.
	 *
	 * @throws IllegalStateException if the type is not Update
	 */
	private void requireUpdateResult() {
		if (type != Type.Update) throw new IllegalStateException("Result must be of Update type");
	}

	/**
	 * Checks if this object is a list of results from a SELECT query.
	 *
	 * @return
	 */
	public boolean isQueryResult() {
		return type == Type.Query;
	}

	/**
	 * Requires the Result object to be of type Query.
	 *
	 * @throws IllegalStateException if the type is not Query
	 */
	private void requireQueryResult() {
		if (type != Type.Query) throw new IllegalStateException("Result must be of Query type");
	}

	/**
	 * Closes the Results, freeing the underlying ResultSet if applicable.
	 *
	 * If this Results is fully consumed by an Iterator or a Stream, this function will
	 * automatically be called. You should not rely on this behavior if it is possible for
	 * the iteration to be stopped before having consumed the whole data set.
	 *
	 * @throws SQLException
	 */
	public void close() {
		if (resultSet != null) {
			try {
				resultSet.close();
			} catch (SQLException e) {
				// Ignore
			}
			resultSet = null;
		}

		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				// Ignore
			}
			statement = null;
		}
	}

	/**
	 * Checks if this Results object has been properly closed and does no longer hold
	 * any internal Closable objects.
	 */
	public boolean isClosed() {
		return resultSet == null;
	}

	//###################################################################
	// QueryStructure implementation
	//###################################################################

	/**
	 * Returns the sources used to generate these results.
	 */
	public List<PersistentStructure<? extends Column>> sources() {
		requireQueryResult();
		throw new UnsupportedOperationException("Not implemented");
	}

	/**
	 * Returns an unmodifiable list of the columns composing these results.
	 * Only application if this Result is a Query result.
	 */
	public List<GeneratedColumn> columns() {
		requireQueryResult();
		return Collections.unmodifiableList(columns);
	}

	/**
	 * Returns the number of columns in this result.
	 */
	public int columnCount() {
		return columns().size();
	}

	/**
	 * Returns the column with the given name
	 * Only application if this Result is a Query result.
	 */
	public Optional<GeneratedColumn> column(String name) {
		requireQueryResult();
		return Optional.ofNullable(columnsIndex.get(name));
	}

	/**
	 * Returns the column at a given index.
	 * Only application if this Result is a Query result.
	 */
	public Optional<GeneratedColumn> column(int idx) {
		requireQueryResult();
		return (idx < 0 || idx >= columns.size()) ? Optional.empty() : Optional.of(columns.get(idx));
	}

	//###################################################################
	// Update result methods
	//###################################################################

	/**
	 * Returns the number of rows updated by the query.
	 */
	public int updateCount() {
		requireUpdateResult();
		return updateCount;
	}

	//###################################################################
	// Query result methods
	//###################################################################

	/**
	 * Indicates if the Result object can be consumed by an Iterator or a Stream pipeline.
	 */
	public boolean canBeConsumed() {
		return type == Type.Query && (!consumed || rows != null);
	}

	/**
	 * Consumes the Result.
	 * This method cannot be called more than one time if row storage is not enabled.
	 */
	private void consume() {
		requireQueryResult();
		if (isClosed() && !done) throw new IllegalStateException("Result object is closed");
		if (!canBeConsumed()) throw new IllegalStateException("Stream has already been consumed");
		consumed = true;
	}

	/**
	 * Enables row storage and returns this Result object.
	 * Once called, this Result can be consumed multiple times.
	 */
	public Result stored() {
		consume();
		rows = new ArrayList<>();
		return this;
	}

	/**
	 * Return the row at the given index (1-based).
	 *
	 * If storage is not enabled, only the row matching the current one from the underlying
	 * ResultSet can be returned. Requesting the next row will advance the ResultSet.
	 *
	 * If storage is enabled, previously fetched rows can still be requested.
	 */
	private synchronized Row row(int idx) {
		if (idx == currentRowIdx) {
			return currentRow != null ? currentRow.view() : null;
		} else if (!done && idx == currentRowIdx + 1) {
			try {
				currentRowIdx++;
				if (resultSet.next()) {
					currentRow = new Row(this, resultSet);
					if (rows != null) rows.add(currentRow);
					return currentRow;
				}
			} catch (SQLException ignored) {}
			currentRow = null;
			done = true;
			close();
			return null;
		} else if (idx < currentRowIdx && rows != null) {
			return rows.get(idx - 1).view();
		} else {
			throw new IllegalStateException("Unordered access to result rows without storage enabled");
		}
	}

	/**
	 * Constructs an iterator allowing to iterate over the rows of this Results set.
	 */
	public ResultIterator iterator() {
		consume();
		return new ResultIterator();
	}

	/**
	 * Iterator over the Rows of this result set.
	 */
	private class ResultIterator implements Iterator<Row> {
		private int current = 0;

		@Override
		public boolean hasNext() {
			return row(current + 1) != null;
		}

		@Override
		public Row next() {
			return row(++current);
		}

		/**
		 * Returns the current position of the iterator
		 */
		public int current() {
			return current;
		}

		/**
		 * Moves the iterator to the requested row
		 */
		public void move(int pos) {
			throw new UnsupportedOperationException("Not implemented");
		}

		public boolean hasPrevious() {
			throw new UnsupportedOperationException("Not implemented");
		}

		public Row previous() {
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	/**
	 * Constructs a Spliterator for this Result
	 */
	public Spliterator<Row> spliterator() {
		int characteristics = Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.DISTINCT;
		return Spliterators.spliteratorUnknownSize(iterator(), characteristics);
	}

	/**
	 * Constructs a stream of rows.
	 */
	public Stream<Row> stream() {
		return StreamSupport.stream(spliterator(), false);
	}

	// Disambiguate the forEach() method.
	@Override
	public void forEach(Consumer<? super Row> action) {
		Iterable.super.forEach(action);
	}
}
