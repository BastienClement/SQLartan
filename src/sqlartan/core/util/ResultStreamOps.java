package sqlartan.core.util;

import sqlartan.core.Row;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface ResultStreamOps extends StreamOps<Row>, AutoCloseable {
	/**
	 * @deprecated Use COUNT() in the SQL query instead
	 */
	@Deprecated
	default long count() { return StreamOps.super.count(); }

	/**
	 * @deprecated Rows are always distinct
	 */
	@Deprecated
	default Stream<Row> distinct() {
		return StreamOps.super.distinct();
	}

	default Stream<Row> filter(Predicate<? super Row> predicate) {
		return stream().filter(predicate);
	}

	/**
	 * @deprecated Use findFirst() instead
	 */
	@Deprecated
	default Optional<Row> findAny() {
		return findFirst();
	}

	default Optional<Row> findFirst() {
		Optional<Row> first = StreamOps.super.findFirst();;
		try {
			close();
		} catch (Exception ignored) {}
		return first;
	}

	/**
	 * @deprecated Use LIMIT in the SQL query instead
	 */
	@Deprecated
	default Stream<Row> limit(long maxSize) {
		return StreamOps.super.limit(maxSize);
	}

	default <U> U reduce(U identity, BiFunction<U, ? super Row, U> accumulator) {
		U result = identity;
		Iterator<Row> it = stream().iterator();
		while (it.hasNext()) {
			result = accumulator.apply(result, it.next());
		}
		return result;
	}

	/**
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
	 * Combination of findFirst() and map()
	 *
	 * @param mapper
	 * @param <R>
	 * @return
	 */
	default <R> Optional<R> mapFirstOptional(Function<? super Row, ? extends R> mapper) {
		return findFirst().map(mapper);
	}

	/**
	 * Combination of findFirst(), map() and Optional::get().
	 *
	 * @param mapper
	 * @param <R>
	 * @return
	 */
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default <R> R mapFirst(Function<? super Row, ? extends R> mapper) {
		return mapFirstOptional(mapper).get();
	}

	/**
	 * Combination of map(), filter(Optional::isPresent) and map(Optional::get).
	 *
	 * @param mapper
	 * @param <R>
	 * @return
	 */
	default <R> Stream<R> mapOptional(Function<? super Row, Optional<R>> mapper) {
		return map(mapper).filter(Optional::isPresent).map(Optional::get);
	}

	/**
	 * Checks if at least one row is available in the Result.
	 *
	 * @return
	 */
	default boolean exists() {
		return findFirst().isPresent();
	}
}
