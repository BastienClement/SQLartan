package sqlartan.core.stream;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Mixin interface that provides required methods for implementing an
 * IterableStream when a class already implements Iterable.
 * This is achieved by first implementing Streamable and then using
 * StreamableAdapter.
 */
public interface IterableAdapter<T> extends StreamableAdapter<T> {
	/**
	 * Returns the characteristics of a Spliterator built from the Iterator
	 * of the implementer. By default, assume IMMUTABLE and ORDERED.
	 */
	default int spliteratorCharacteristics() {
		return Spliterator.IMMUTABLE | Spliterator.ORDERED;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Spliterator<T> spliterator() {
		return Spliterators.spliteratorUnknownSize(iterator(), spliteratorCharacteristics());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default Stream<T> stream() {
		return StreamSupport.stream(spliterator(), false).onClose(this::close);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	default boolean isReiterable() {
		return true;
	}

	//
	// Remove default implementation from StreamableAdapter
	//
	// This prevents accidentally creating an infinite loop:
	//
	//     /--> IterableAdapter.spliterator()
	//     | -> StreamableAdapter.iterator()
	//     | -> IterableAdapter.stream() --\
	//     \-------------------------------/
	//
	/**
	 * {@inheritDoc}
	 */
	@Override
	Iterator<T> iterator();

	/**
	 * {@inheritDoc}
	 */
	@Override
	default void close() {}
}
