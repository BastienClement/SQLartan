package sqlartan.utils;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utilities for fixing broken Java optionals
 */
public abstract class Optionals {
	/**
	 * Returns the first optional with value from a list of suppliers.
	 *
	 * @param suppliers a list of suppliers of optional values
	 * @param <T>       the most-common type of the returned value
	 */
	@SuppressWarnings("unchecked")
	@SafeVarargs
	public static <T> Optional<T> firstPresent(Supplier<Optional<? extends T>>... suppliers) {
		Optional<? extends T> opt = null;
		for (Supplier<Optional<? extends T>> supplier : suppliers) {
			opt = supplier.get();
			if (opt.isPresent()) break;
		}
		return opt == null ? Optional.empty() : (Optional<T>) opt;
	}
}
