package sqlartan.core.stream;

import java.util.stream.Stream;

/**
 * A stream()'able object
 *
 * @param <T> the type of elements in the stream
 */
public interface Streamable<T> {
	Stream<T> stream();
}
