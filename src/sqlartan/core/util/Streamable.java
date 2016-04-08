package sqlartan.core.util;

import java.util.stream.Stream;

/**
 * An object that can be streamed as a Java 8 Stream.
 *
 * @param <T> the type of elements in the stream
 */
public interface Streamable<T> {
	Stream<T> stream();
}
