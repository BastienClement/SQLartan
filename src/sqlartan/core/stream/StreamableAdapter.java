package sqlartan.core.stream;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

/**
 * Mixin interface that implements the IterableStream interface on instances of Streamable.
 */
public interface StreamableAdapter<T> extends Streamable<T>, IterableStream<T> {
	@Override
	default boolean isReiterable() {
		return true;
	}

	//
	// Custom operations
	//

	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator) {
		U result = identity;
		for (T e : this) {
			result = accumulator.apply(result, e);
		}
		return result;
	}

	@Override
	default <R> Optional<R> mapFirstOptional(Function<? super T, ? extends R> mapper) {
		return findFirst().map(mapper);
	}

	@Override
	@SuppressWarnings("OptionalGetWithoutIsPresent")
	default <R> R mapFirst(Function<? super T, ? extends R> mapper) {
		return mapFirstOptional(mapper).get();
	}

	@Override
	default <R> IterableStream<R> mapOptional(Function<? super T, Optional<R>> mapper) {
		return IterableStream.from(stream().map(mapper).filter(Optional::isPresent).map(Optional::get));
	}

	//
	// Stream methods
	//

	@Override
	default boolean allMatch(Predicate<? super T> predicate) {
		return stream().allMatch(predicate);
	}

	@Override
	default boolean anyMatch(Predicate<? super T> predicate) {
		return stream().anyMatch(predicate);
	}

	@Override
	default <R, A> R collect(Collector<? super T, A, R> collector) {
		return stream().collect(collector);
	}

	@Override
	default <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
		return stream().collect(supplier, accumulator, combiner);
	}

	@Override
	default long count() {
		return stream().count();
	}

	@Override
	default IterableStream<T> distinct() {
		return IterableStream.from(stream().distinct());
	}

	@Override
	default IterableStream<T> filter(Predicate<? super T> predicate) {
		return IterableStream.from(stream().filter(predicate));
	}

	@Override
	default Optional<T> findAny() {
		return stream().findAny();
	}

	@Override
	default Optional<T> findFirst() {
		return stream().findFirst();
	}

	@Override
	default <R> IterableStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return IterableStream.from(stream().flatMap(mapper));
	}

	@Override
	default DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
		return stream().flatMapToDouble(mapper);
	}

	@Override
	default IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
		return stream().flatMapToInt(mapper);
	}

	@Override
	default LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
		return stream().flatMapToLong(mapper);
	}

	@Override
	default void forEach(Consumer<? super T> action) {
		stream().forEach(action);
	}

	@Override
	default void forEachOrdered(Consumer<? super T> action) {
		stream().forEachOrdered(action);
	}

	@Override
	default IterableStream<T> limit(long maxSize) {
		return IterableStream.from(stream().limit(maxSize));
	}

	@Override
	default <R> IterableStream<R> map(Function<? super T, ? extends R> mapper) {
		return IterableStream.from(stream().map(mapper));
	}

	@Override
	default DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
		return stream().mapToDouble(mapper);
	}

	@Override
	default IntStream mapToInt(ToIntFunction<? super T> mapper) {
		return stream().mapToInt(mapper);
	}

	@Override
	default LongStream mapToLong(ToLongFunction<? super T> mapper) {
		return stream().mapToLong(mapper);
	}

	@Override
	default Optional<T> max(Comparator<? super T> comparator) {
		return stream().max(comparator);
	}

	@Override
	default Optional<T> min(Comparator<? super T> comparator) {
		return stream().min(comparator);
	}

	@Override
	default boolean noneMatch(Predicate<? super T> predicate) {
		return stream().noneMatch(predicate);
	}

	@Override
	default IterableStream<T> peek(Consumer<? super T> action) {
		return IterableStream.from(stream().peek(action));
	}

	@Override
	default Optional<T> reduce(BinaryOperator<T> accumulator) {
		return stream().reduce(accumulator);
	}

	@Override
	default T reduce(T identity, BinaryOperator<T> accumulator) {
		return stream().reduce(identity, accumulator);
	}

	@Override
	default <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
		return stream().reduce(identity, accumulator, combiner);
	}

	@Override
	default IterableStream<T> skip(long n) {
		return IterableStream.from(stream().skip(n));
	}

	@Override
	default IterableStream<T> sorted() {
		return IterableStream.from(stream().sorted());
	}

	@Override
	default IterableStream<T> sorted(Comparator<? super T> comparator) {
		return IterableStream.from(stream().sorted(comparator));
	}

	@Override
	default Object[] toArray() {
		return stream().toArray();
	}

	@Override
	default <A> A[] toArray(IntFunction<A[]> generator) {
		return stream().toArray(generator);
	}

	//
	// BaseStream methods
	//

	@Override
	default void close() {
		stream().close();
	}

	@Override
	default boolean isParallel() {
		return stream().isParallel();
	}

	@Override
	default Iterator<T> iterator() {
		return stream().iterator();
	}

	@Override
	default IterableStream<T> onClose(Runnable closeHandler) {
		return IterableStream.from(stream().onClose(closeHandler));
	}

	@Override
	default IterableStream<T> parallel() {
		return IterableStream.from(stream().parallel());
	}

	@Override
	default IterableStream<T> sequential() {
		return IterableStream.from(stream().sequential());
	}

	@Override
	default Spliterator<T> spliterator() {
		return stream().spliterator();
	}

	@Override
	default IterableStream<T> unordered() {
		return IterableStream.from(stream().unordered());
	}
}
