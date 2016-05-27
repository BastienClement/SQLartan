package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import sqlartan.core.stream.IterableAdapter;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.QueryResolver;
import sqlartan.core.util.UncheckedSQLException;
import sqlartan.util.Lazy;
import java.sql.*;
import java.util.*;
import static sqlartan.util.Lazy.lazy;

/**
 * The result of a database query.
 *
 * This class represents both the result of SELECT-type query and the result
 * of an UPDATE-type query. But the actual type of the result can only be one
 * of the two sub-types for a given query.
 */
public abstract class Result implements ReadOnlyResult, AutoCloseable, IterableStream<Row> {
	/**
	 * Constructs a Result by executing the given query on the connection.
	 *
	 * @param connection the connection on which the query must be executed
	 * @param query      the SQL query
	 * @return the result set returned by the database
	 * @throws SQLException if the SQL query is invalid
	 */
	static Result fromQuery(Database database, Connection connection, String query) throws SQLException {
		Statement statement = connection.createStatement();
		return from(database, statement, statement.execute(query), query);
	}

	/**
	 * Constructs a Result by executing the given prepared statement.
	 *
	 * @param statement the prepared statement to execute
	 * @return the result set returned by the database
	 * @throws SQLException if the SQL query is invalid
	 */
	static Result fromPreparedStatement(Database database, PreparedStatement statement, String sql) throws SQLException {
		return from(database, statement, statement.execute(), sql);
	}

	/**
	 * Constructs a Result by reading the result of the given statement.
	 *
	 * @param statement the statement instance on which the request
	 *                  was executed
	 * @param query     a flag indicating if the request is a SELECT
	 *                  or an UPDATE statement
	 * @return the result set returned by the database
	 * @throws SQLException if the SQL query is invalid
	 */
	private static Result from(Database database, Statement statement, boolean query, String sql) throws SQLException {
		return query ? new QueryResult(database, statement, sql) : new UpdateResult(database, statement, sql);
	}

	/**
	 * The source database
	 */
	private Database database;

	/**
	 * The underlying statement object
	 */
	private Statement statement;

	/**
	 * The source SQL query
	 */
	private String sql;

	/**
	 * Constructs a Result for the given query.
	 *
	 * @param database  the parent database
	 * @param statement the statement on which the query was executed
	 * @param sql       the source SQL query
	 */
	private Result(Database database, Statement statement, String sql) {
		this.database = database;
		this.statement = statement;
		this.sql = sql;
	}

	/**
	 * Returns the database from which this result was generated.
	 *
	 * @return the parent database
	 */
	public Database database() {
		return database;
	}

	/**
	 * Returns the SQL query that generated this result set.
	 *
	 * @return the source SQL query
	 */
	public String query() {
		return sql;
	}

	/**
	 * Checks if this object is a list of results from a SELECT query.
	 *
	 * @return true if this object is a query result
	 */
	public boolean isQueryResult() { return false; }

	/**
	 * Checks if this object is a result of an UPDATE or DELETE query.
	 *
	 * Also returns true if the query was a DDL statement, but updateCount()
	 * will always be 0 in this case.
	 *
	 * @return true if this object is an update result
	 */
	public boolean isUpdateResult() { return false; }

	/**
	 * Closes the Results, freeing the underlying ResultSet if applicable.
	 *
	 * If this Results is fully consumed by an Iterator or a Stream, this
	 * function will automatically be called. You should not rely on this
	 * behavior if it is possible for the iteration to be stopped before
	 * having consumed the whole data set.
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
	 *
	 * @return the number of rows updated
	 */
	public int updateCount() {
		throw new UnsupportedOperationException("This Result is not an UpdateResult");
	}

	//###################################################################
	// Query result methods
	//###################################################################

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ImmutableList<ResultColumn> columns() {
		throw new UnsupportedOperationException("This Result is not a QueryResult");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ResultColumn> column(String name) {
		throw new UnsupportedOperationException("This Result is not a QueryResult");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ResultColumn> column(int idx) {
		throw new UnsupportedOperationException("This Result is not a QueryResult");
	}

	/**
	 * Result of an SELECT-like statement.
	 */
	static class QueryResult extends Result implements IterableAdapter<Row> {
		/**
		 * The columns of this result set
		 */
		private ArrayList<ResultColumn> columns;

		/**
		 * A map of the columns in this result set indexed by name.
		 */
		private Map<String, ResultColumn> columnsIndex;

		/**
		 * The underlying JDBC result set
		 */
		private ResultSet resultSet;

		/**
		 * Whether this result set was already consumed once
		 */
		private boolean consumed = false;

		/**
		 * Constructs a QueryResult by reading the given Statement object.
		 *
		 * @param statement the source statement object
		 * @throws SQLException if an error occurs while reading the results.
		 */
		private QueryResult(Database database, Statement statement, String sql) throws SQLException {
			super(database, statement, sql);
			resultSet = statement.getResultSet();

			// Read metadata
			ResultSetMetaData meta = resultSet.getMetaData();
			int count = meta.getColumnCount();

			columns = new ArrayList<>(count);
			columnsIndex = new HashMap<>();

			for (int i = 1; i <= count; i++) {
				int index = i;
				String name = meta.getColumnName(i);
				String table = meta.getTableName(i);
				String type = meta.getColumnTypeName(i);
				boolean nullable = meta.isNullable(i) == 1;

				ResultColumn col = new ResultColumn(this, index, new ResultColumn.Properties() {
					public String name() { return name; }
					public String type() { return type; }
					public Optional<Table> sourceTable() { return database.table(table); }
					public Optional<TableColumn> sourceColumn() { return columnRef(index); }
					public boolean nullable() { return nullable; }
				});

				columns.add(col);
				columnsIndex.put(name, col);
			}
		}

		/**
		 * A list of the TableColumn composing this result set.
		 */
		private Lazy<Optional<ImmutableList<TableColumn>>> columnRefs =
			lazy(() -> QueryResolver.resolveColumns(database(), query()));

		/**
		 * Returns the source table column for a given column in the
		 * result set.
		 *
		 * @param index the index of the column in the result set
		 * @return the corresponding table column
		 */
		private Optional<TableColumn> columnRef(int index) {
			return columnRefs.get().flatMap(c -> c.skip(index - 1).findFirst());
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isQueryResult() { return true; }

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ImmutableList<ResultColumn> columns() {
			return ImmutableList.from(columns);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<ResultColumn> column(String name) {
			return Optional.ofNullable(columnsIndex.get(name));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<ResultColumn> column(int idx) {
			return (idx < 0 || idx >= columns.size()) ? Optional.empty() : Optional.of(columns.get(idx));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int spliteratorCharacteristics() {
			return IterableAdapter.super.spliteratorCharacteristics() | Spliterator.NONNULL | Spliterator.DISTINCT;
		}

		/**
		 * Constructs an iterator allowing to iterate over the rows of
		 * this Results set. Only one such iterator can be created.
		 */
		public Iterator<Row> iterator() {
			if (isClosed()) throw new IllegalStateException("Result object is closed");
			if (consumed) throw new IllegalStateException("Stream has already been consumed");
			consumed = true;
			return new ResultIterator();
		}

		/**
		 * Iterator over the rows of this result set.
		 */
		private class ResultIterator implements Iterator<Row> {
			/**
			 * The current row
			 */
			private Row current;

			/**
			 * Constructs a new ResultIterator.
			 * Also fetches the first row of the result set.
			 */
			private ResultIterator() {
				fetch();
			}

			/**
			 * Fetches the next row form the result set.
			 */
			private void fetch() {
				try {
					current = resultSet.next() ? new Row(QueryResult.this, resultSet) : null;
					if (current == null) close();
				} catch (SQLException e) {
					throw new UncheckedSQLException(e);
				}
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean hasNext() {
				return current != null;
			}

			/**
			 * Returns the current row, then fetches the next one.
			 * {@inheritDoc}
			 */
			@Override
			public Row next() {
				try {
					return current;
				} finally {
					fetch();
				}
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ImmutableList<Row> toList() {
			return ImmutableList.from(toArray(Row[]::new), Row::view);
		}

		/**
		 * @return true
		 */
		@Override
		public boolean isReiterable() {
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
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

		/**
		 * A list of unique columns in this result set
		 */
		private Lazy<Optional<ImmutableList<ResultColumn>>> uniqueColumns =
			new Lazy<Optional<ImmutableList<ResultColumn>>>() {
				private int idx;

				@Override
				public Optional<ImmutableList<ResultColumn>> gen() {
					idx = 0;
					return QueryResolver.resolveColumns(database(), query()).map(cols ->
						cols.map(c -> column(idx++).orElseThrow(IllegalStateException::new))
						    .filter(c -> c.sourceColumn().orElseThrow(IllegalStateException::new).unique())
					);
				}
			};

		/**
		 * Returns a list of the unique columns in the result set.
		 * This methods only provides a result for simple select statements.
		 *
		 * @return A list of the unique columns in the result set, if
		 * computable.
		 */
		public Optional<ImmutableList<ResultColumn>> uniqueColumns() {
			return uniqueColumns.get();
		}
	}

	/**
	 * The result of an UPDATE-like statement.
	 * This also includes every DDL statements.
	 *
	 * This kind of result set automatically closes every underlying JDBC resources
	 * once created. There is no need to call .close() on it.
	 */
	private static class UpdateResult extends Result implements IterableAdapter<Row> {
		/**
		 * Number of updated rows
		 */
		private int updateCount = 0;

		/**
		 * Constructs an UpdateResult object.
		 *
		 * @param database  the parent database
		 * @param statement the JDBC statement of the query
		 * @param sql       the source SQL query
		 * @throws SQLException if JDBC is broken
		 */
		private UpdateResult(Database database, Statement statement, String sql) throws SQLException {
			super(database, statement, sql);
			updateCount = statement.getUpdateCount();
			close();
		}

		/**
		 * @return true
		 */
		@Override
		public boolean isUpdateResult() { return true; }

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int updateCount() {
			return updateCount;
		}

		/**
		 * An UpdateResult cannot be iterated.
		 *
		 * @throws UnsupportedOperationException if called
		 */
		@Override
		public Iterator<Row> iterator() {
			throw new UnsupportedOperationException();
		}
	}
}

