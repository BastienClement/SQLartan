package sqlartan.core.util;

import java.util.stream.Stream;

public interface Streamable<T> {
	Stream<T> stream();
}
