package sqlartan.core.stream;

import java.util.Comparator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A sequence of elements that implements both Stream and Iterable.
 *
 * Some instances of IterableStream can be iterated or consumed multiple
 * times if the underlying source allows it.
 *
 * If a class already implements Streamable, this interface can be implemented
 * by implementing StreamableAdapter instead.
 *
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
	 * Constructs a new IterableStream from a Supplier of independent Stream instances.
	 * The returned IterableStream can be iterated or consumed multiple times.
	 *
	 * @param supplier a Supplier of independent Stream instances
	 * @param <U>      the type of elements in the stream
	 */
	static <U> IterableStream<U> from(Supplier<Stream<U>> supplier) {
		return (StreamableAdapter<U>) supplier::get;
	}

	/**
	 * Returns true if this IterableStream can be iterated or consumed multiple times.
	 */
	boolean isReiterable();

	/**
	 * Returns a reiterable copy of this IterableStream.
	 * If this stream is already reiterable the same object is returned, else a new
	 * IterableStream is constructed if one does not already exists for this object.
	 *
	 * Calling this method on a non-reiterable IterableStream will consume the original
	 * stream and constructs an ImmutableList containing all elements from the stream.
	 */
	default IterableStream<T> reiterable() {
		return isReiterable() ? this : ImmutableList.from(this);
	}

	/**
	 * Transforms this IterableStream to an ImmutableList.
	 * ImmutableList also implements the IterableStream but is guaranteed to be reiterable,
	 * implements the List interface and offers random access to elements.
	 */
	default ImmutableList<T> toList() {
		return ImmutableList.from(this);
	}

	/**
	 * Returns a view of this object.
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
	 *
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
	<R> IterableStream<R> mapOptional(Function<? super T, Optional<R>> mapper);

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it, if any.
	 *
	 * Combination of findFirst() and map()
	 *
	 * @param mapper the mapping function
	 * @param <R>    the type of the result
	 */
	<R> Optional<R> mapFirstOptional(Function<? super T, ? extends R> mapper);

	/**
	 * Returns the first element of this stream after applying the mapper
	 * function to it. If the stream is empty, an exception is thrown.
	 *
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

	//
	// Stream methods
	//

	@Override
	IterableStream<T> distinct();

	@Override
	IterableStream<T> filter(Predicate<? super T> predicate);

	@Override
	<R> IterableStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

	@Override
	IterableStream<T> limit(long maxSize);

	@Override
	<R> IterableStream<R> map(Function<? super T, ? extends R> mapper);

	@Override
	IterableStream<T> peek(Consumer<? super T> action);

	@Override
	IterableStream<T> skip(long n);

	@Override
	IterableStream<T> sorted();

	@Override
	IterableStream<T> sorted(Comparator<? super T> comparator);

	//
	// BaseStream methods
	//

	@Override
	IterableStream<T> onClose(Runnable closeHandler);

	@Override
	IterableStream<T> parallel();

	@Override
	IterableStream<T> sequential();

	@Override
	IterableStream<T> unordered();

	//
	// Disambiguate methods by removing default implementations
	//

	@Override
	void forEach(Consumer<? super T> action);

	@Override
	Spliterator<T> spliterator();
}

