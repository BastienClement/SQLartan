package sqlartan.core;

import sqlartan.core.util.RowStreamOps;
import sqlartan.core.util.Streamable;
import java.sql.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class Results implements QueryStructure<GeneratedColumn>, Iterable<Row>, Streamable<Row>, RowStreamOps, AutoCloseable {
	/** Type of the Results object */
	private enum Type {
		Update, Query
	}

	private Statement statement;
	private Results.Type type;

	private int updateCount = 0;

	private ArrayList<GeneratedColumn> columns;
	private HashMap<String, GeneratedColumn> columnsIndex;

	private ResultSet resultSet;
	private Row row;

	Results(Connection connection, String query) throws SQLException {
		statement = connection.createStatement();
		if (statement.execute(query)) {
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
		return type == Type.Query;
	}

	/**
	 *
	 * @return
	 */
	public boolean isConsumed() {
		return row != null;
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

	/**
	 * Inits the row field.
	 */
	private void initRow() {
		if (type != Type.Query) throw new IllegalStateException("Results must be of Query type");
		if (row != null) throw new IllegalStateException("Stream has already been consumed");
		row = new Row(this, resultSet);
	}

	/**
	 * Constructs an iterator allowing to iterate over the rows of this Results set.
	 *
	 * @return
	 */
	@Override
	public Iterator<Row> iterator() {
		this.initRow();
		return new ResultsIterator(row);
	}

	/**
	 * Constructs a stream of rows.
	 *
	 * @return
	 */
	public Stream<Row> stream() {
		int characteristics = Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.NONNULL;
		Spliterator<Row> split = Spliterators.spliteratorUnknownSize(this.iterator(), characteristics);
		return StreamSupport.stream(split, false);
	}

	/**
	 *
	 * @param mapper
	 * @param <R>
	 * @return
	 */
	@Override
	public <R> Optional<R> firstOptional(Function<? super Row, ? extends R> mapper) {
		Optional<R> res = RowStreamOps.super.firstOptional(mapper);
		try {
			close();
		} catch (SQLException e) {
			// Ignore ?
		}
		return res;
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
	@Override
	public void close() throws SQLException {
		if (resultSet != null) {
			resultSet.close();
			resultSet = null;
		}

		if (statement != null) {
			statement.close();
			statement = null;
		}
	}

	/**
	 * Checks if this Results object has been properly closed and does no longer hold
	 * any internal Closable objects.
	 *
	 * @return
	 */
	public boolean isClosed() {
		return resultSet == null;
	}

	/**
	 * Indicates if a next row is available in the ResultsIterator.
	 * Since inner classes cannot declare static structure, it is declared here instead.
	 *
	 * [No]
	 * There is no more rows in the ResultSet.
	 *
	 * [Yes]
	 * There is at least one more row in the Result set.
	 *
	 * [Maybe]
	 * It is unknown whether there is a next row or not.
	 * next() will be called on the ResultSet to change the state to Yes or No.
	 */
	private enum HasNext {
		No,
		Yes,
		Maybe
	}

	/**
	 * Iterator over the Rows of this result set.
	 */
	private class ResultsIterator implements Iterator<Row> {
		/**
		 * Keep track of the state of the underlying ResultSet.
		 * The ternary logic used here is to prevent multiple calls to hasNext() to
		 * advance the ResultSet of more than one row.
		 */
		private HasNext hasNext = HasNext.Maybe;

		/**
		 * The Row object that will be returned by next().
		 */
		private Row row;

		/**
		 * @param row The Row object to return from next()
		 */
		private ResultsIterator(Row row) {
			this.row = row;
		}

		/**
		 * Checks if there is a next row to iterate on.
		 *
		 * The first time this function returns false, the iterated Results object will be
		 * automatically closed. This means that if you iterate over the whole set of results,
		 * there is no need to manually call the close() method or use try-with-resource block.
		 *
		 * @return true if there is a row left to iterate on, false otherwise
		 */
		@Override
		public boolean hasNext() {
			if (hasNext == HasNext.Maybe) {
				try {
					if (resultSet.next()) {
						hasNext = HasNext.Yes;
						row.reset();
					} else {
						hasNext = HasNext.No;
						close();
					}
				} catch (SQLException e) {
					throw new IllegalStateException("Unable to fetch next row", e);
				}
			}
			return hasNext == HasNext.Yes;
		}

		/**
		 * Returns the next row from the Results object.
		 *
		 * @return the next row from the Results set
		 * @throws IllegalStateException if there is no more rows available
		 */
		@Override
		public Row next() {
			if (hasNext != HasNext.Yes) throw new IllegalStateException("HasNext: " + hasNext);
			hasNext = HasNext.Maybe;
			return row;
		}
	}
}
