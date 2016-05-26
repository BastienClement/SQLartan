package sqlartan.util;

import java.util.Optional;
import java.util.function.Supplier;

public class Lazy<T> {
	private Supplier<T> generator;
	private boolean generated = false;
	private T value;

	public Lazy(Supplier<T> generator) {
		this.generator = generator;
	}

	public T gen() {
		return generator.get();
	}

	public synchronized T get() {
		if (!generated) {
			value = gen();
			generated = true;
		}
		return value;
	}

	public synchronized Optional<T> opt() {
		return generated ? Optional.of(value) : Optional.empty();
	}
}
