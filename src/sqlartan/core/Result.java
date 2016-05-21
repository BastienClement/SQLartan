package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableAdapter;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.*;
import java.util.*;

/**
 * The result of a database query.
 *
 * TODO: write more
 */
public abstract class Result implements QueryStructure<GeneratedColumn>, AutoCloseable, IterableStream<Row> {
	/**
	 * Constructs a Result by executing the given query on the connection.
	 *
	 * @param connection the connection to execute the query on
	 * @param query      the SQL query
	 * @throws SQLException
	 */
	public static Result fromQuery(Connection connection, String query) throws SQLException {
		Statement statement = connection.createStatement();
		return from(statement, statement.execute(query));
	}

	/**
	 * Constructs a Result by executing the given prepared statement.
	 *
	 * @param preparedStatement the prepared statement to execute
	 * @throws SQLException
	 */
	public static Result fromPreparedStatement(PreparedStatement preparedStatement)
			throws SQLException {
		return from(preparedStatement, preparedStatement.execute());
	}

	/**
	 * Constructs a Result by reading the result of the given statement.
	 *
	 * @param statement the statement instance on which the request was executed
	 * @param query     a flag indicating if the request was a SELECT or UPDATE statement
	 * @throws SQLException
	 */
	private static Result from(Statement statement, boolean query) throws SQLException {
		return query ? new QueryResult(statement) : new UpdateResult(statement);
	}

	/** The underlying statement object */
	private Statement statement;

	private Result(Statement statement) {
		this.statement = statement;
	}

	/**
	 * Checks if this object is a list of results from a SELECT query.
	 */
	public boolean isQueryResult() { return false; }

	/**
	 * Checks if this object is a result of an UPDATE or DELETE query.
	 * Returns true if the query was a DDL statement, but updateCount() will always return 0 in this case.
	 */
	public boolean isUpdateResult() { return false; }

	/**
	 * Closes the Results, freeing the underlying ResultSet if applicable.
	 *
	 * If this Results is fully consumed by an Iterator or a Stream, this function will
	 * automatically be called. You should not rely on this behavior if it is possible for
	 * the iteration to be stopped before having consumed the whole data set.
	 */
	public void close() {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException ignored) {}
			statement = null;
		}
	}

	/**
	 * Checks if this Results object has been properly closed and does no longer hold
	 * any internal Closable objects.
	 */
	public boolean isClosed() {
		return statement == null;
	}

	//###################################################################
	// Update result methods
	//###################################################################

	/**
	 * Returns the number of rows updated by the query.
	 */
	public int updateCount() {
		throw new UnsupportedOperationException();
	}

	//###################################################################
	// Query result methods
	//###################################################################

	@Override
	public IterableStream<PersistentStructure<? extends Column>> sources() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ImmutableList<GeneratedColumn> columns() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<GeneratedColumn> column(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<GeneratedColumn> column(int idx) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Indicates if this Result can be consumed by an Iterator or a Stream pipeline.
	 */
	public boolean canBeConsumed() { return false; }

	/**
	 * Result of an SELECT-like statement
	 * TODO: write more
	 */
	private static class QueryResult extends Result implements IterableAdapter<Row> {
		private ArrayList<GeneratedColumn> columns;
		private HashMap<String, GeneratedColumn> columnsIndex;

		private ResultSet resultSet;
		private boolean consumed = false;

		/**
		 * Constructs a QueryResult by reading the given Statement object.
		 * TODO: write more
		 *
		 * @param statement
		 * @throws SQLException
		 */
		private QueryResult(Statement statement) throws SQLException {
			super(statement);
			resultSet = statement.getResultSet();

			// Read metadata
			ResultSetMetaData meta = resultSet.getMetaData();
			int count = meta.getColumnCount();

			columns = new ArrayList<>(count);
			columnsIndex = new HashMap<>();

			for (int i = 1; i <= count; i++) {
				String name = meta.getColumnName(i);
				String table = meta.getTableName(i);
				String type = meta.getColumnTypeName(i);
				boolean nullable = meta.isNullable(i) == 1;

				GeneratedColumn col = new GeneratedColumn(new GeneratedColumn.Properties() {
					public String name() { return name; }
					public String type() {
						return type;
					}
					public String sourceTable() { return table; }
					public String sourceExpr() { throw new UnsupportedOperationException(); }
					public boolean nullable() { return nullable; }
				});

				columns.add(col);
				columnsIndex.put(name, col);
			}
		}

		@Override
		public boolean isQueryResult() { return true; }

		@Override
		public ImmutableList<PersistentStructure<? extends Column>> sources() {
			throw new UnsupportedOperationException("Not implemented");
		}

		@Override
		public ImmutableList<GeneratedColumn> columns() {
			return ImmutableList.from(columns);
		}

		@Override
		public Optional<GeneratedColumn> column(String name) {
			return Optional.ofNullable(columnsIndex.get(name));
		}

		@Override
		public Optional<GeneratedColumn> column(int idx) {
			return (idx < 0 || idx >= columns.size()) ? Optional.empty() : Optional.of(columns.get(idx));
		}

		@Override
		public boolean canBeConsumed() { return !consumed && !isClosed(); }

		/**
		 * TODO
		 */
		private void consume() {
			if (isClosed()) throw new IllegalStateException("Result object is closed");
			if (!canBeConsumed()) throw new IllegalStateException("Stream has already been consumed");
			consumed = true;
		}

		@Override
		public int spliteratorCharacteristics() {
			return IterableAdapter.super.spliteratorCharacteristics() | Spliterator.NONNULL | Spliterator.DISTINCT;
		}

		/**
		 * Constructs an iterator allowing to iterate over the rows of this Results set.
		 */
		public Iterator<Row> iterator() {
			consume();
			return new ResultIterator();
		}

		/**
		 * Iterator over the Rows of this result set.
		 */
		private class ResultIterator implements Iterator<Row> {
			private Row current;

			private ResultIterator() {
				fetch();
			}

			/**
			 * TODO
			 */
			private void fetch() {
				try {
					current = resultSet.next() ? new Row(QueryResult.this, resultSet) : null;
					if (current == null) close();
				} catch (SQLException e) {
					throw new UncheckedSQLException(e);
				}
			}

			@Override
			public boolean hasNext() {
				return current != null;
			}

			@Override
			public Row next() {
				Row row = current;
				fetch();
				return row;
			}
		}

		@Override
		public ImmutableList<Row> toList() {
			return ImmutableList.from(toArray(Row[]::new), Row::view);
		}

		@Override
		public boolean isReiterable() {
			return false;
		}

		@Override
		public void close() {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException ignored) {}
				resultSet = null;
			}

			super.close();
		}
	}

	/**
	 * Result of an UPDATE-like statement
	 * TODO: write more
	 */
	private static class UpdateResult extends Result implements IterableAdapter<Row> {
		private int updateCount = 0;

		private UpdateResult(Statement statement) throws SQLException {
			super(statement);
			updateCount = statement.getUpdateCount();
			close();
		}

		@Override
		public boolean isUpdateResult() { return true; }

		@Override
		public int updateCount() {
			return updateCount;
		}

		@Override
		public Iterator<Row> iterator() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Checks if at least one row is available in the Result.
	 */
	public boolean exists() {
		return findFirst().isPresent();
	}
}

