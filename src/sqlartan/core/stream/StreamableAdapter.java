package sqlartan.core.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Mixin interface that implements the IterableStream interface on
 * instances of Streamable.
 * <p>
 * Proxy methods that does not return an IterableStream will also call
 * close() on the stream returned by the stream() method.
 */
public interface StreamableAdapter<T> extends Streamable<T>, IterableStream<T> {
	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean isReiterable() {
		return true;
	}

	//
	// Custom operations
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
		U result = identity;
		for (T e : this) {
			result = accumulator.apply(result, e);
		}
		this.close();
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R> Optional<R> mapFirstOptional(Function<? super T, ? extends R> mapper) {
		return findFirst().map(mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default <R> R mapFirst(Function<? super T, ? extends R> mapper) {
		return mapFirstOptional(mapper).get();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R> IterableStream<R> mapOptional(Function<? super T, Optional<R>> mapper) {
		return IterableStream.from(stream().map(mapper).filter(Optional::isPresent).map(Optional::get));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> find(Predicate<? super T> predicate) {
		try (IterableStream<T> self = this) {
			for (T e : self) {
				if (predicate.test(e)) {
					return Optional.of(e);
				}
			}
			return Optional.empty();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean exists(Predicate<? super T> predicate) {
		return find(predicate).isPresent();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean exists() {
		return findAny().isPresent();
	}

	//
	// Stream methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean allMatch(Predicate<? super T> predicate) {
		try (Stream<T> stream = stream()) {
			return stream.allMatch(predicate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean anyMatch(Predicate<? super T> predicate) {
		try (Stream<T> stream = stream()) {
			return stream.anyMatch(predicate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		try (Stream<T> stream = stream()) {
			return stream.collect(collector);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		try (Stream<T> stream = stream()) {
			return stream.collect(supplier, accumulator, combiner);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default long count() {
		try (Stream<T> stream = stream()) {
			return stream.count();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> distinct() {
		return IterableStream.from(stream().distinct());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> filter(Predicate<? super T> predicate) {
		return IterableStream.from(stream().filter(predicate));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> findAny() {
		try (Stream<T> stream = stream()) {
			return stream.findAny();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> findFirst() {
		try (Stream<T> stream = stream()) {
			return stream.findFirst();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R> IterableStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return IterableStream.from(stream().flatMap(mapper));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use flatMap().
	 */
	@Override
	@Deprecated
	default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use flatMap().
	 */
	@Override
	@Deprecated
	default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return stream().flatMapToInt(mapper);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use flatMap().
	 */
	@Override
	@Deprecated
	default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return stream().flatMapToLong(mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void forEach(Consumer<? super T> action) {
		try (Stream<T> stream = stream()) {
			stream.forEach(action);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void forEachOrdered(Consumer<? super T> action) {
		try (Stream<T> stream = stream()) {
			stream.forEachOrdered(action);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> limit(long maxSize) {
		return IterableStream.from(stream().limit(maxSize));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <R> IterableStream<R> map(Function<? super T, ? extends R> mapper) {
		return IterableStream.from(stream().map(mapper));
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use map().
	 */
	@Override
	@Deprecated
	default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream().mapToDouble(mapper);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use map().
	 */
	@Override
	@Deprecated
	default IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream().mapToInt(mapper);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Escape from IterableStream wrapper, use map().
	 */
	@Override
	@Deprecated
	default LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream().mapToLong(mapper);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		try (Stream<T> stream = stream()) {
			return stream.max(comparator);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		try (Stream<T> stream = stream()) {
			return stream.min(comparator);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean noneMatch(Predicate<? super T> predicate) {
		try (Stream<T> stream = stream()) {
			return stream.noneMatch(predicate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> peek(Consumer<? super T> action) {
		return IterableStream.from(stream().peek(action));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Optional<T> reduce(BinaryOperator<T> accumulator) {
		try (Stream<T> stream = stream()) {
			return stream.reduce(accumulator);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default T reduce(T identity, BinaryOperator<T> accumulator) {
		try (Stream<T> stream = stream()) {
			return stream.reduce(identity, accumulator);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		try (Stream<T> stream = stream()) {
			return stream.reduce(identity, accumulator, combiner);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> skip(long n) {
		return IterableStream.from(stream().skip(n));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> sorted() {
		return IterableStream.from(stream().sorted());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> sorted(Comparator<? super T> comparator) {
		return IterableStream.from(stream().sorted(comparator));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Object[] toArray() {
		try (Stream<T> stream = stream()) {
			return stream.toArray();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		try (Stream<T> stream = stream()) {
			return stream.toArray(generator);
		}
	}

	//
	// BaseStream methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void close() {
		stream().close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean isParallel() {
		return stream().isParallel();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> onClose(Runnable closeHandler) {
		return IterableStream.from(stream().onClose(closeHandler));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> parallel() {
		return IterableStream.from(stream().parallel());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> sequential() {
		return IterableStream.from(stream().sequential());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Spliterator<T> spliterator() {
		return stream().spliterator();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default IterableStream<T> unordered() {
		return IterableStream.from(stream().unordered());
	}
}
