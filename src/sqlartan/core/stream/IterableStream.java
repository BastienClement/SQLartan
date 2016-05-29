package sqlartan.core.stream;

import java.util.Comparator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A sequence of elements that implements both Stream and Iterable.
 * <p>
 * Some instances of IterableStream can be iterated or consumed multiple
 * times if the underlying source allows it.
 * <p>
 * If a class already implements Streamable, this interface can be implemented
 * by implementing StreamableAdapter instead.
 * <p>
 * If a class already implements Iterable, this interface can be implemented
 * by implementing IterableAdapter instead.
 *
 * @param <T> the type of elements in this stream
 */
public interface IterableStream<T> extends Stream<T>, Iterable<T> {
	/**
	 * Constructs a new IterableStream wrapping the given Stream instance.
	 * The returned IterableStream can be iterated or consumed a single time only.
	 *
	 * @param stream the stream to wrap
	 * @param <U>    the type of elements in the stream
	 */
	static <U> IterableStream<U> from(Stream<U> stream) {
		return new StreamableAdapter<U>() {
			@Override
			public Stream<U> stream() {
				return stream;
			}

			@Override
			public boolean isReiterable() {
				return false;
			}
		};
	}

	/**
	 * Constructs a new IterableStream wrapping the given Iterable instance.
	 * The returned IterableStream can be iterated or consumed multiple times.
	 *
	 * @param iterable an iterable to wrap
	 * @param <U>      the type of elements from the iterable
	 */
	static <U> IterableStream<U> from(Iterable<U> iterable) {
		return (IterableAdapter<U>) iterable::iterator;
	}

	/**
	 * Constructs a new IterableStream from a Supplier of independent Stream
	 * instances. The returned IterableStream can be iterated or consumed
	 * multiple times.
	 *
	 * @param supplier a Supplier of independent Stream instances
	 * @param <U>      the type of elements in the stream
	 */
	static <U> IterableStream<U> from(Supplier<Stream<U>> supplier) {
		return (StreamableAdapter<U>) supplier::get;
	}

	/**
	 * Concatenates two IterableStream.
	 * <p>
	 * The resulting stream will contain every elements from the first stream
	 * followed by elements from the second.
	 *
	 * @param a   the first stream to concatenate
	 * @param b   the second stream to concatenate
	 * @param <U> the super-type of both stream
	 */
	static <U> IterableStream<U> concat(IterableStream<? extends U> a, IterableStream<? extends U> b) {
		return IterableStream.from(Stream.concat(a, b));
	}

	/**
	 * Returns true if this IterableStream can be iterated or consumed multiple times.
	 */
	boolean isReiterable();

	/**
	 * Returns a reiterable copy of this IterableStream.
	 * <p>
	 * If this stream is already reiterable the same object is returned,
	 * else a new IterableStream is constructed.
	 * <p>
	 * Calling this method on a non-reiterable IterableStream will consume
	 * the original stream and constructs an ImmutableList containing every
	 * elements from the stream.
	 */
	default IterableStream<T> reiterable() {
		return isReiterable() ? this : ImmutableList.from(this);
	}

	/**
	 * Transforms this IterableStream to an ImmutableList.
	 * <p>
	 * ImmutableList also implements the IterableStream but is guaranteed
	 * to be reiterable, implements the List interface and offers random
	 * access to elements.
	 */
	default ImmutableList<T> toList() {
		return ImmutableList.from(this);
	}

	/**
	 * Returns a view of this object.
	 * <p>
	 * A view is guaranteed to be backed by a Stream pipeline and not a List.
	 * If called on a Stream, returns itself.
	 */
	default IterableStream<T> view() { return this; }

	//
	// Custom operations
	//

	/**
	 * Performs a reduction on the elements of this stream, using the provided
	 * identity and accumulation functions.
	 * <p>
	 * This function is an alternative to the `T reduce(T, BinaryOperator<T>)`
	 * method with a different type for the result but no combiner function.
	 *
	 * @param identity    the identity value for the accumulation function
	 * @param accumulator an associative, non-interfering, stateless function
	 *                    for incorporating an additional element into a result
	 * @param <U>         the type of the result
	 */
	<U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator);

	/**
	 * Performs a map/filter in a single operation.
	 * <p>
	 * The mapping function must returns Optionals of the intended result type
	 * that either contains a value or are empty. Empty optionals are then
	 * filtered and the remaining values are unwrapped to produce the result
	 * stream.
	 * <p>
	 * Combination of map(), filter(Optional::isPresent), map(Optional::get).
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	<R> IterableStream<R> mapOptional(Function<? super T, Optional<R>> mapper);

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it, if any.
	 * <p>
	 * Combination of findFirst(), map().
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	<R> Optional<R> mapFirstOptional(Function<? super T, ? extends R> mapper);

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it. If the stream is empty, an exception is thrown.
	 * <p>
	 * Combination of findFirst(), map() and Optional::get()
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	<R> R mapFirst(Function<? super T, ? extends R> mapper);

	/**
	 * Finds the first element of the stream satisfying a predicate, if any.
	 *
	 * @param predicate a predicate function
	 */
	Optional<T> find(Predicate<? super T> predicate);

	/**
	 * Tests whether a predicate holds for some of the elements of this stream.
	 *
	 * @param predicate a predicate function
	 * @return true if the predicate holds for at least one element
	 */
	boolean exists(Predicate<? super T> predicate);

	/**
	 * Tests whether there is at least one element in this stream.
	 *
	 * @return true if there is at least one elements
	 */
	boolean exists();

	//
	// Stream methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> distinct();

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> filter(Predicate<? super T> predicate);

	/**
	 * {@inheritDoc}
	 */
	@Override
	<R> IterableStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> limit(long maxSize);

	/**
	 * {@inheritDoc}
	 */
	@Override
	<R> IterableStream<R> map(Function<? super T, ? extends R> mapper);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> peek(Consumer<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> skip(long n);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> sorted();

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> sorted(Comparator<? super T> comparator);

	//
	// BaseStream methods
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> onClose(Runnable closeHandler);

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> parallel();

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> sequential();

	/**
	 * {@inheritDoc}
	 */
	@Override
	IterableStream<T> unordered();

	//
	// Disambiguate methods by removing default implementations
	//

	/**
	 * {@inheritDoc}
	 */
	@Override
	void forEach(Consumer<? super T> action);

	/**
	 * {@inheritDoc}
	 */
	@Override
	Spliterator<T> spliterator();
}

