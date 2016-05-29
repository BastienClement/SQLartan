package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import java.util.Optional;

/**
 * The readonly interface of a Result object.
 * <p>
 * This interface is used as the parameter type for execute listeners.
 * Since results set cannot be consumed multiple times, this interfaces
 * ensures that listeners will not attempt to consume it.
 */
public interface ReadOnlyResult extends Structure<ResultColumn> {
	/**
	 * Returns the SQL query that generated this result set.
	 *
	 * @return the source SQL query
	 */
	String query();

	/**
	 * Checks if this object is a list of results from a SELECT query.
	 *
	 * @return true if this object is a query result
	 */
	boolean isQueryResult();

	/**
	 * Checks if this object is a result of an UPDATE or DELETE query.
	 * <p>
	 * Also returns true if the query was a DDL statement, but updateCount()
	 * will always be 0 in this case.
	 *
	 * @return true if this object is an update result
	 */
	boolean isUpdateResult();

	/**
	 * Returns the number of rows updated by the query.
	 *
	 * @return the number of rows updated
	 *
	 * @throws UnsupportedOperationException if called on the result of a
	 *                                       SELECT-like query
	 */
	int updateCount();

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException if called on the result of a
	 *                                       UPDATE-like query
	 */
	@Override
	ImmutableList<ResultColumn> columns();

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException if called on the result of a
	 *                                       UPDATE-like query
	 */
	@Override
	Optional<ResultColumn> column(String name);

	/**
	 * {@inheritDoc}
	 *
	 * @throws UnsupportedOperationException if called on the result of a
	 *                                       UPDATE-like query
	 */
	@Override
	Optional<ResultColumn> column(int idx);
}
