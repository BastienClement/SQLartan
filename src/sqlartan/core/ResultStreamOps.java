package sqlartan.core;

import sqlartan.core.util.StreamOps;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Mixin interface that provides Stream operation to the Result object.
 */
interface ResultStreamOps extends StreamOps<Row>, AutoCloseable {
	/**
	 * Returns the number of items in the stream.
	 * Calling this method will consume the whole stream to count how many items are in it.
	 *
	 * @deprecated Use COUNT() in the SQL query instead
	 */
	@Deprecated
	default long count() { return StreamOps.super.count(); }

	/**
	 * Checks if every objects from the stream are distinct (according to Object.equals).
	 * Since Row objects are always distinct from one another, this method has no point in this context.
	 *
	 * @deprecated Rows are always distinct
	 */
	@Deprecated
	default Stream<Row> distinct() {
		return StreamOps.super.distinct();
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
	 * the Result object freeing underlying JDBC resources.
	 */
	default Optional<Row> findFirst() {
		Optional<Row> first = StreamOps.super.findFirst();
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
	default Stream<Row> limit(long n) {
		return StreamOps.super.limit(n);
	}

	/**
	 * Performs a reduction on the elements of this stream, using the provided
	 * identity and accumulation functions.
	 *
	 * This function is an alternative to the `T reduce(T, BinaryOperator<T>)`
	 * method with a different type for the result but no combiner function.
	 *
	 * @param identity    the identity value for the accumulation function
	 * @param accumulator an associative, non-interfering, stateless function
	 *                    for incorporating an additional element into a result
	 * @param <U>         the type of the result
	 */
	default <U> U reduce(U identity, BiFunction<U, ? super Row, U> accumulator) {
		U result = identity;
		Iterator<Row> it = stream().iterator();
		while (it.hasNext()) {
			result = accumulator.apply(result, it.next());
		}
		return result;
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
	default Stream<Row> skip(long n) {
		return StreamOps.super.skip(n);
	}

	//###################################################################
	// Custom additions
	//###################################################################

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it, if any.
	 *
	 * Combination of findFirst() and map()
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	default <R> Optional<R> mapFirstOptional(Function<? super Row, ? extends R> mapper) {
		return findFirst().map(mapper);
	}

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it. If the stream is empty, an exception is thrown.
	 *
	 * Combination of findFirst(), map() and Optional::get()
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default <R> R mapFirst(Function<? super Row, ? extends R> mapper) {
		return mapFirstOptional(mapper).get();
	}

	/**
	 * Performs a map/filter in a single operation.
	 *
	 * The mapping function must returns Optionals of the intended result type
	 * that either contains a value or are empty. Empty optionals are then filtered
	 * and the remaining values are unwrapped to produce the result stream.
	 *
	 * Combination of map(), filter(Optional::isPresent) and map(Optional::get).
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	default <R> Stream<R> mapOptional(Function<? super Row, Optional<R>> mapper) {
		return map(mapper).filter(Optional::isPresent).map(Optional::get);
	}

	/**
	 * Checks if at least one row is available in the Result.
	 */
	default boolean exists() {
		return findFirst().isPresent();
	}
}
