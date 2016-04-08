package sqlartan.core.util;

import sqlartan.core.Row;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.*;

public interface RowStreamOps extends Streamable<Row>, AutoCloseable {
	default boolean allMatch(Predicate<? super Row> predicate) {
		return stream().allMatch(predicate);
	}

	default boolean anyMatch(Predicate<? super Row> predicate) {
		return stream().anyMatch(predicate);
	}

	default <R, A> R collect(Collector<? super Row, A, R> collector) {
		return stream().collect(collector);
	}

	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super Row> accumulator, BiConsumer<R, R> combiner) {
		return stream().collect(supplier, accumulator, combiner);
	}

	/**
	 * @deprecated Use COUNT() in the SQL query instead
	 */
	@Deprecated
	default long count() {
		return stream().count();
	}

	/**
	 * @deprecated Rows are always distinct
	 */
	@Deprecated
	default Stream<Row> distinct() {
		return stream().distinct();
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
		Optional<Row> first = stream().findFirst();
		try {
			close();
		} catch (Exception ignored) {}
		return first;
	}

	default <R> Stream<R> flatMap(Function<? super Row, ? extends Stream<? extends R>> mapper) {
		return stream().flatMap(mapper);
	}

	default DoubleStream flatMapToDouble(Function<? super Row, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	default IntStream flatMapToInt(Function<? super Row, ? extends IntStream> mapper) {
		return stream().flatMapToInt(mapper);
	}

	default LongStream flatMapToLong(Function<? super Row, ? extends LongStream> mapper) {
		return stream().flatMapToLong(mapper);
	}

	/**
	 * @deprecated Use LIMIT in the SQL query instead
	 */
	@Deprecated
	default Stream<Row> limit(long maxSize) {
		return stream().limit(maxSize);
	}

	default <R> Stream<R> map(Function<? super Row, ? extends R> mapper) {
		return stream().map(mapper);
	}

	default DoubleStream mapToDouble(ToDoubleFunction<? super Row> mapper) {
		return stream().mapToDouble(mapper);
	}

	default IntStream mapToInt(ToIntFunction<? super Row> mapper) {
		return stream().mapToInt(mapper);
	}

	default LongStream mapToLong(ToLongFunction<? super Row> mapper) {
		return stream().mapToLong(mapper);
	}

	default Optional<Row> max(Comparator<? super Row> comparator) {
		return stream().max(comparator);
	}

	default Optional<Row> min(Comparator<? super Row> comparator) {
		return stream().min(comparator);
	}

	default boolean noneMatch(Predicate<? super Row> predicate) {
		return stream().noneMatch(predicate);
	}

	default Stream<Row> peek(Consumer<? super Row> action) {
		return stream().peek(action);
	}

	default <U> U reduce(U identity, BiFunction<U, ? super Row, U> accumulator, BinaryOperator<U> combiner) {
		return stream().reduce(identity, accumulator, combiner);
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
		return stream().skip(n);
	}

	default Stream<Row> sorted(Comparator<? super Row> comparator) {
		return stream().sorted(comparator);
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
