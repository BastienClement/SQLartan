package sqlartan.util;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @param <T>
 */
public abstract class Lazy<T> {
	/**
	 *
	 * @param generator
	 * @param <U>
	 * @return
	 */
	public static <U> Lazy<U> lazy(Supplier<U> generator) {
		return new OfGenerator<>(generator);
	}

	/**
	 *
	 */
	private boolean generated = false;

	/**
	 *
	 */
	private T value;

	/**
	 *
	 * @return
	 */
	public abstract T gen();

	/**
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
	 *
	 * @return
	 */
	public synchronized Optional<T> opt() {
		return generated ? Optional.of(value) : Optional.empty();
	}

	/**
	 * @param <T>
	 */
	private static class OfGenerator<T> extends Lazy<T> {
		/**
		 *
		 */
		private Supplier<T> generator;

		/**
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
