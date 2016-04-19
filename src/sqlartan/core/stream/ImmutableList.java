package sqlartan.core.stream;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * An immutable List of elements that directly supports Stream operations.
 *
 * Stream operations preserves the ImmutableList type and are strict, creating a
 * complete copy of transformed elements at each step.
 *
 * @param <T> the type of elements in this list
 */
public interface ImmutableList<T> extends List<T>, StreamableAdapter<T>, RandomAccess {
	/**
	 * Constructs an immutable list with the given array of elements.
	 * The array will be cloned to ensure immutability.
	 *
	 * @param elements an array of elements
	 * @param <U>      the type of elements in the stream
	 */
	@SafeVarargs
	static <U> ImmutableList<U> from(U... elements) {
		return ImmutableListImpl.from(elements);
	}

	/**
	 * Constructs an immutable list by copying every elements from the given collection.
	 *
	 * @param collection a collection of elements
	 * @param <U>        the type of elements in the collection
	 */
	@SuppressWarnings("unchecked")
	static <U> ImmutableList<U> from(Collection<U> collection) {
		return ImmutableListImpl.wrap((U[]) collection.toArray());
	}

	/**
	 * Constructs an immutable list by collecting every elements from the given stream.
	 *
	 * @param stream a stream of elements
	 * @param <U>    the type of elements in the stream
	 */
	@SuppressWarnings("unchecked")
	static <U> ImmutableList<U> from(Stream<U> stream) {
		return ImmutableListImpl.wrap((U[]) stream.toArray());
	}

	/**
	 * Constructs an immutable list from an array of elements and a mapper function.
	 * The mapper function will be called each time an element in accessed from the list.
	 * The given array will be stored as-is. Care must be taken to ensure it is effectively immutable.
	 *
	 * @param elements an immutable array of elements
	 * @param mapper   a mapping function
	 * @param <U>      the type of elements from the array
	 * @param <R>      the type returned by the mapper function
	 */
	static <U, R> ImmutableList<R> from(U[] elements, Function<? super U, ? extends R> mapper) {
		return ImmutableListImpl.mapping(elements, mapper);
	}

	@Override
	default ImmutableList<T> toList() {
		return this;
	}

	/**
	 * Returns an IterableStream over this list.
	 * The intended usage is to create transform pipeline that do not construct intermediate
	 * immutable list at each step.
	 */
	@Override
	default IterableStream<T> view() {
		return IterableStream.from(this::stream);
	}

	@Override
	default Stream<T> stream() {
		return List.super.stream();
	}

	@Override
	default Spliterator<T> spliterator() {
		return List.super.spliterator();
	}

	@Override
	Object[] toArray();

	@Override
	Iterator<T> iterator();

	//
	// Override transforming IterableStream methods to return ImmutableList instead
	//

	@Override
	default ImmutableList<T> distinct() {
		return ImmutableList.from(stream().distinct());
	}

	@Override
	default ImmutableList<T> filter(Predicate<? super T> predicate) {
		return ImmutableList.from(stream().filter(predicate));
	}

	@Override
	default <R> ImmutableList<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
		return ImmutableList.from(stream().flatMap(mapper));
	}

	@Override
	default ImmutableList<T> limit(long maxSize) {
		return ImmutableList.from(stream().limit(maxSize));
	}

	@Override
	default <R> ImmutableList<R> map(Function<? super T, ? extends R> mapper) {
		return ImmutableList.from(stream().map(mapper));
	}

	@Override
	default ImmutableList<T> peek(Consumer<? super T> action) {
		return ImmutableList.from(stream().peek(action));
	}

	@Override
	default ImmutableList<T> skip(long n) {
		return ImmutableList.from(stream().skip(n));
	}

	@Override
	default ImmutableList<T> sorted() {
		return ImmutableList.from(stream().sorted());
	}

	@Override
	default ImmutableList<T> sorted(Comparator<? super T> comparator) {
		return ImmutableList.from(stream().sorted(comparator));
	}

	@Override
	default <R> ImmutableList<R> mapOptional(Function<? super T, Optional<R>> mapper) {
		return ImmutableList.from(stream().map(mapper).filter(Optional::isPresent).map(Optional::get));
	}
}
