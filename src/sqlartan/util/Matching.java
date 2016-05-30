package sqlartan.util;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Pattern matching helper
 * <p>
 * To use this class:
 * import static sqlartan.util.Matching.match;
 * import static sqlartan.util.Matching.dispatch;
 *
 * @param <T> Type of the value matched against
 */
public class Matching<T> {
	/**
	 * A predicate that always hold
	 */
	private static Predicate<Object> truth = z -> true;

	/**
	 * Match expression against the given value.
	 * <p>
	 * The return type of the expression will be given by the first .when() case.
	 *
	 * @param value the value to match against
	 * @param <T>   The type of the value
	 */
	public static <T> Matching<T> match(T value) {
		return new Matching<>(value);
	}

	/**
	 * Match expression against the given value.
	 * <p>
	 * The return type of the expression is given by the target argument.
	 *
	 * @param value  the value to match against
	 * @param target the type of the result to return
	 * @param <T>    The type of the value
	 * @param <R>    The type of the result
	 */
	public static <T, R> Matching<T>.Returning<R> match(T value, Class<R> target) {
		return new Matching<>(value).new Returning<>();
	}

	/**
	 * Dispatch expression against the given value.
	 * <p>
	 * Does not return a value.
	 *
	 * @param value the value to match against
	 * @param <T>   The type of the value
	 */
	public static <T> Matching<T>.Void dispatch(T value) {
		return new Matching<>(value).new Void();
	}

	/**
	 * The value being matched
	 */
	private final T value;

	/**
	 * The class of the value
	 */
	private final Class<?> valueClass;

	/*
	 * @param value the value being matched
	 */
	private Matching(T value) {
		this.value = value;
		this.valueClass = value != null ? value.getClass() : null;
	}

	/**
	 * Checks if the given class is a match with the class of the value.
	 *
	 * @param matchClass the class to check for match
	 * @return true if the value is assignable to the given class
	 */
	private boolean isMatch(Class<?> matchClass) {
		return valueClass != null && matchClass.isAssignableFrom(valueClass);
	}

	/**
	 * Checks if the given value is considered equals to the one being matched.
	 *
	 * @param o the value to test for equality
	 * @return true if the value is considered equals
	 */
	private boolean isEqual(Object o) {
		return value == o || (value != null && value.equals(o));
	}

	/**
	 * Matching case.
	 * <p>
	 * If the value matches the given class, expr is executed and its returned
	 * value is the result of the match expression.
	 *
	 * @param matchClass the class to match against
	 * @param expr       an expression to execute if this case is a match
	 * @param <M>        the type of the match case
	 * @param <R>        the return type of the expression
	 * @return a returning matching expression
	 */
	public <M, R> Returning<R> when(Class<M> matchClass, Function<? super M, ? extends R> expr) {
		return when(matchClass, truth, expr);
	}

	/**
	 * Matching case.
	 * <p>
	 * If the value matches the given class and the given predicate, expr is
	 * executed and the returned value is the result of the match expression.
	 *
	 * @param matchClass the class to match against
	 * @param predicate  a predicate that must hold for this case to be a match
	 * @param expr       an expression to execute if this case is a match
	 * @param <M>        the type of the match case
	 * @param <R>        the return type of the expression
	 * @return a returning matching expression
	 */
	public <M, R> Returning<R> when(Class<M> matchClass, Predicate<? super M> predicate, Function<? super M, ? extends R> expr) {
		return new Returning<R>().when(matchClass, predicate, expr);
	}

	/**
	 * Matching case.
	 * <p>
	 * If the value is equals to the given value, expr is executed and the
	 * returned value is the result of the match expression.
	 *
	 * @param matchValue the value to match against
	 * @param expr       an expression to execute if this case is a match
	 * @param <U>        the type of the maatch case
	 * @param <R>        the return type of the expression
	 * @return a returning matching expression
	 */
	public <U, R> Returning<R> when(U matchValue, Supplier<? extends R> expr) {
		return new Returning<R>().when(matchValue, expr);
	}

	/**
	 * A matching expression returning a value
	 *
	 * @param <R> The return type of this matching
	 */
	public class Returning<R> {
		// I HATE YOU JAVA!
		// Y U NO <S super R>?
		//public <M extends T, S super R> Returning<S> when(Class<M> matchClass, Function<? super M, ? extends S> expr)

		/**
		 * Matching case
		 * <p>
		 * If the value matches the given class, expr is executed and its
		 * returned value is the result of the match expression.
		 *
		 * @param matchClass the class to match against
		 * @param expr       expression to execute if this case is a match
		 * @param <M>        Type of the match case
		 */
		public <M> Returning<R> when(Class<M> matchClass, Function<? super M, ? extends R> expr) {
			return when(matchClass, truth, expr);
		}

		/**
		 * Matching case
		 * <p>
		 * If the value matches the given class, expr is executed and its
		 * returned value is the result of the match expression.
		 *
		 * @param matchClass the class to match against
		 * @param pred       a predicate that must hold for this case to be
		 *                   considered a match
		 * @param expr       expression to execute if this case is a match
		 * @param <M>        Type of the match case
		 */
		@SuppressWarnings("unchecked")
		public <M> Returning<R> when(Class<M> matchClass, Predicate<? super M> pred, Function<? super M, ? extends R> expr) {
			if (isMatch(matchClass) && pred.test((M) value)) {
				return new MatchedReturning<>(expr.apply((M) value));
			} else {
				return this;
			}
		}

		/**
		 * Matching case.
		 * <p>
		 * If the value is equals to the given value, expr is executed and the
		 * returned value is the result of the match expression.
		 *
		 * @param matchValue the value to match against
		 * @param expr       an expression to execute if this case is a match
		 * @param <U>        the type of the maatch case
		 * @return a returning matching expression
		 */
		public <U> Returning<R> when(U matchValue, Supplier<? extends R> expr) {
			if (isEqual(matchValue)) {
				return new MatchedReturning<>(expr.get());
			} else {
				return this;
			}
		}

		/**
		 * Returns the value of this match expression or an empty optional if
		 * none of the cases matched.
		 */
		public Optional<R> get() {
			return Optional.empty();
		}

		/**
		 * Returns the value of this match expression or `other` if none of
		 * the cases matched.
		 */
		public R orElse(R other) {
			return other;
		}

		/**
		 * Returns the value of this match expression or the value from the
		 * supplier if none of the cases matched.
		 */
		public R orElse(Supplier<? extends R> other) {
			return other.get();
		}

		/**
		 * Returns the value of this match expression or throws an exception
		 * if none of the cases matched.
		 */
		public <Z extends Throwable> R orElseThrow(Supplier<? extends Z> supplier) throws Z {
			throw supplier.get();
		}

		/**
		 * Returns the value of this match expression or throws an exception
		 * if none of the cases matched.
		 *
		 * @throws NoSuchElementException if none of the cases were a match
		 */
		public final R orElseThrow() {
			return orElseThrow(NoSuchElementException::new);
		}
	}

	/**
	 * A matched expression.
	 * <p>
	 * This class exposes the same API as Returning, but no more cases
	 * will be tested for matching.
	 *
	 * @param <R> the type of result of the match expression
	 */
	private class MatchedReturning<R> extends Returning<R> {
		/**
		 * The result of the match expression
		 */
		private final R result;

		/**
		 * @param result the result of the match expression
		 */
		private MatchedReturning(R result) {
			this.result = result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <M> Returning<R> when(Class<M> matchClass, Function<? super M, ? extends R> expr) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <M> Returning<R> when(Class<M> matchClass, Predicate<? super M> pred, Function<? super M, ? extends R> expr) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Returning<R> when(U matchValue, Supplier<? extends R> expr) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Optional<R> get() {
			return Optional.ofNullable(result);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public R orElse(R other) {
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public R orElse(Supplier<? extends R> other) {
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <Z extends Throwable> R orElseThrow(Supplier<? extends Z> supplier) throws Z {
			return result;
		}
	}

	/**
	 * A dispatch expression
	 */
	public class Void {
		/**
		 * Dispatch case.
		 * <p>
		 * If the value matches the given class, expr is executed.
		 *
		 * @param matchClass the class to match against
		 * @param expr       expression to execute if this case is a match
		 * @param <M>        type of the match case
		 */
		public <M> Void when(Class<M> matchClass, Consumer<? super M> expr) {
			return when(matchClass, truth, expr);
		}

		/**
		 * Dispatch case.
		 * <p>
		 * If the value matches the given class, expr is executed.
		 *
		 * @param matchClass the class to match against
		 * @param pred       a predicate that must hold for this case to be
		 *                   considered a match
		 * @param expr       expression to execute if this case is a match
		 * @param <M>        type of the match case
		 */
		@SuppressWarnings("unchecked")
		public <M> Void when(Class<M> matchClass, Predicate<? super M> pred, Consumer<? super M> expr) {
			if (isMatch(matchClass) && pred.test((M) value)) {
				expr.accept((M) value);
				return new MatchedVoid();
			} else {
				return this;
			}
		}

		/**
		 * Dispatch case.
		 * <p>
		 * If the value matches the given value, expr is executed.
		 *
		 * @param matchValue the value to match against
		 * @param expr       expression to execute if this case is a match
		 * @param <U>        type of the value
		 */
		public <U> Void when(U matchValue, Runnable expr) {
			if (isEqual(matchValue)) {
				expr.run();
				return new MatchedVoid();
			} else {
				return this;
			}
		}

		/**
		 * If none of the previous cases were a match, executes action.
		 *
		 * @param action the action to execute
		 */
		public void orElse(Runnable action) {
			action.run();
		}

		/**
		 * If none of the previous cases were a match, throws the exception
		 * returned by the supplier.
		 *
		 * @param supplier a supplier of Throwable instances
		 * @param <Z>      the type of thrown exception
		 * @throws Z if none of the cases were a match
		 */
		public <Z extends Throwable> void orElseThrow(Supplier<? extends Z> supplier) throws Z {
			throw supplier.get();
		}

		/**
		 * If none of the previous cases were a match, throws an exception.
		 *
		 * @throws NoSuchElementException if none of the cases were a match
		 */
		public final void orElseThrow() {
			orElseThrow(NoSuchElementException::new);
		}
	}

	/**
	 * A dummy dispatch expression that was already matched
	 */
	private class MatchedVoid extends Void {
		/**
		 * {@inheritDoc}
		 */
		@Override
		public <M> Void when(Class<M> matchClass, Predicate<? super M> pred, Consumer<? super M> expr) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <U> Void when(U matchValue, Runnable expr) {
			return this;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void orElse(Runnable action) {}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public <Z extends Throwable> void orElseThrow(Supplier<? extends Z> supplier) throws Z {}
	}
}
