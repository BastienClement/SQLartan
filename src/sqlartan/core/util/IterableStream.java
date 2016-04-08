package sqlartan.core.util;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IterableStream<T> implements StreamOps<T>, Iterable<T> {
	public static <U> IterableStream<U> of(Stream<U> stream) {
		return new IterableStream<>(stream);
	}

	private Stream<T> stream;

	public IterableStream(Stream<T> stream) {
		this.stream = stream;
	}

	public Stream<T> stream() {
		return stream;
	}

	public Iterator<T> iterator() {
		return stream.iterator();
	}

	public void forEach(Consumer<? super T> action) {
		stream.forEach(action);
	}

	public List<T> toList() {
		return stream.collect(Collectors.toList());
	}

	@SuppressWarnings("unchecked")
	public T[] toArray() {
		return (T[]) stream.toArray();
	}
}
