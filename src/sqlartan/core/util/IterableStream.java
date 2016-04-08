package sqlartan.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A wrapper around Java 8's Stream interface that provide some additional
 * features and actually implements Iterable in contrast to the former.
 *
 * @param <T> the type of elements from this stream
 */
public class IterableStream<T> implements StreamOps<T>, Iterable<T> {
	/**
	 * Constructs a new IterableStream from an existing Stream instance.
	 *
	 * @param stream the original stream instance to be wrapped
	 * @param <T>    the type of elements from the stream
	 */
	public static <T> IterableStream<T> of(Stream<T> stream) {
		return new IterableStream<>(stream);
	}

	/** The inner Stream object */
	private Stream<T> stream;

	/**
	 * Constructor for the IterableStream class.
	 * Use IterableStream.of() instead.
	 */
	private IterableStream(Stream<T> stream) {
		this.stream = stream;
	}

	/**
	 * Returns the inner Stream object.
	 */
	public Stream<T> stream() {
		return stream;
	}

	/**
	 * Returns an iterator over each elements in the stream.
	 */
	public Iterator<T> iterator() {
		return stream.iterator();
	}

	/**
	 * Executes the given action for each element in this stream.
	 *
	 * @param action the action to invoke for each element in this stream
	 */
	public void forEach(Consumer<? super T> action) {
		stream.forEach(action);
	}

	/**
	 * Converts this stream to a List.
	 */
	public List<T> toList() {
		return stream.collect(Collectors.toList());
	}

	/**
	 * Converts this stream to an Array.
	 * In contrast to Stream.toArray() the type of the stream is preserved
	 * by using an unchecked cast to T[].
	 */
	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return (T[]) stream.toArray();
	}
}
