package sqlartan.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * TODO
 *
 * @param <T>
 */
public abstract class Lazy<T> {
	/**
	 * TODO
	 *
	 * @param generator
	 * @param <U>
	 * @return
	 */
	public static <U> Lazy<U> lazy(Supplier<U> generator) {
		return new OfGenerator<>(generator);
	}

	/**
	 * TODO
	 */
	private boolean generated = false;

	/**
	 * TODO
	 */
	private T value;

	/**
	 * TODO
	 *
	 * @return
	 */
	public abstract T gen();

	/**
	 * TODO
	 *
	 * @return
	 */
	public synchronized T get() {
		if (!generated) {
			value = gen();
			generated = true;
		}
		return value;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public synchronized Optional<T> opt() {
		return generated ? Optional.of(value) : Optional.empty();
	}

	/**
	 * TODO
	 *
	 * @param <T>
	 */
	private static class OfGenerator<T> extends Lazy<T> {
		/**
		 * TODO
		 */
		private Supplier<T> generator;

		/**
		 * TODO
		 *
		 * @param generator
		 */
		private OfGenerator(Supplier<T> generator) {
			this.generator = generator;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public T gen() {
			return generator.get();
		}
	}
}
