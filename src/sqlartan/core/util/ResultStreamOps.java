package sqlartan.core.util;

import sqlartan.core.Row;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.stream.StreamableAdapter;
import java.util.Optional;

/**
 * Mixin interface that provides additional Stream operations to the Result object.
 */
public interface ResultStreamOps extends StreamableAdapter<Row>, AutoCloseable {
	/**
	 * Returns the number of items in the stream.
	 * Calling this method will consume the whole stream to count how many items are in it.
	 *
	 * @deprecated Use COUNT() in the SQL query instead
	 */
	@Deprecated
	default long count() { return StreamableAdapter.super.count(); }

	/**
	 * Checks if every objects from the stream are distinct (according to Object.equals).
	 * Since Row objects are always distinct from one another, this method has no point in this context.
	 *
	 * @deprecated Rows are always distinct
	 */
	@Deprecated
	default IterableStream<Row> distinct() {
		return StreamableAdapter.super.distinct();
	}

	/**
	 * Returns any element from the Stream, if it is not empty.
	 * Since Result object are purely sequential, this method is identical
	 * to Result.findFirst().
	 *
	 * @deprecated Use findFirst() instead
	 */
	@Deprecated
	default Optional<Row> findAny() {
		return findFirst();
	}

	/**
	 * Returns the first element from the Stream, if it is not empty.
	 * In addition to its defined behavior, this method also closes
	 * the Result object, freeing underlying JDBC resources.
	 */
	default Optional<Row> findFirst() {
		Optional<Row> first = StreamableAdapter.super.findFirst();
		try {
			close();
		} catch (Exception ignored) {}
		return first;
	}

	/**
	 * Returns a new Stream limited to the first n elements of the original stream.
	 *
	 * Since the limited stream may be shorter than the original one, it is no longer
	 * guaranteed that fully consuming the new stream will automatically close and
	 * free JDBC resources. For this reason, it is a better idea to LIMIT the number
	 * of rows from the SQL query and fully consume the original stream.
	 *
	 * TODO: return a hooked stream able to monitor consumed elements and close
	 * the original one once n elements are consumed.
	 *
	 * @deprecated Use LIMIT in the SQL query instead
	 */
	@Deprecated
	default IterableStream<Row> limit(long n) {
		return StreamableAdapter.super.limit(n);
	}

	/**
	 * Returns a stream consisting of the remaining elements of this stream
	 * after discarding the first n elements of the stream.
	 *
	 * This method will actually consume the first n elements of the Result.
	 * Using LIMIT in the SQL query will yield better performances.
	 *
	 * @deprecated Use LIMIT in the SQL query instead
	 */
	@Deprecated
	default IterableStream<Row> skip(long n) {
		return StreamableAdapter.super.skip(n);
	}

	//###################################################################
	// Custom additions
	//###################################################################

	/**
	 * Checks if at least one row is available in the Result.
	 */
	default boolean exists() {
		return findFirst().isPresent();
	}
}
