package sqlartan.core.util;

import sqlartan.core.Row;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public interface RowStreamOps extends Streamable<Row>, AutoCloseable {
	default boolean allMatch(Predicate<? super Row> predicate) {
		return stream().allMatch(predicate);
	}

	default boolean anyMatch(Predicate<? super Row> predicate) {
		return stream().anyMatch(predicate);
	}

	default Stream<Row> filter(Predicate<? super Row> predicate) {
		return stream().filter(predicate);
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

	default boolean noneMatch(Predicate<? super Row> predicate) {
		return stream().noneMatch(predicate);
	}

	default Stream<Row> peek(Consumer<? super Row> action) {
		return stream().peek(action);
	}

	default <U> U reduce(U identity, BiFunction<U, ? super Row, U> accumulator) {
		U result = identity;
		Iterator<Row> it = stream().iterator();
		while (it.hasNext()) {
			result = accumulator.apply(result, it.next());
		}
		return result;
	}

	default <R> Optional<R> firstOptional(Function<? super Row, ? extends R> mapper) {
		return stream().findFirst().map(mapper);
	}

	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default <R> R first(Function<? super Row, ? extends R> mapper) {
		return firstOptional(mapper).get();
	}

	default <R> Stream<R> mapOptional(Function<? super Row, Optional<? extends R>> mapper) {
		return stream().map(mapper).filter(Optional::isPresent).map(Optional::get);
	}
}
