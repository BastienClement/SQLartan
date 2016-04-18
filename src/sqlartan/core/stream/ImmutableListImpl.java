package sqlartan.core.stream;

import java.util.AbstractList;
import java.util.function.Function;

/**
 * Concrete implementation of the ImmutableList interface.
 * This implementation uses a simple object array as storage.
 *
 * @param <T> the type of elements in the list
 */
abstract class ImmutableListImpl<T, U> extends AbstractList<T> implements ImmutableList<T> {
	/**
	 * Constructs a new list from an array of elements.
	 * The array will be cloned to ensure immutability.
	 *
	 * @param elements an array of elements to clone
	 * @param <U>      the type of elements in the array
	 */
	@SuppressWarnings("unchecked")
	static <U> ImmutableList<U> from(U[] elements) {
		Object[] clone = new Object[elements.length];
		System.arraycopy(elements, 0, clone, 0, elements.length);
		return new Simple<>((U[]) clone);
	}

	/**
	 * Constructs a new list from an array of elements.
	 * The array will be used as-is. Care must be taken to ensure it is effectively immutable.
	 *
	 * @param elements an immutable array of elements
	 * @param <U>      the type of elements in the array
	 */
	static <U> ImmutableList<U> wrap(U[] elements) {
		return new Simple<>(elements);
	}

	/**
	 * Constructs a new list from an array of elements and a mapping function.
	 * Every time an elements from the list is accessed, the mapping function will be applied to it.
	 * The given array will be used as-is. Care must be taken to ensure it is effectively immutable.
	 *
	 * @param elements an immutable array
	 * @param mapper   a mapper function
	 * @param <U>      the type of elements from the array
	 * @param <R>      the return type of the mapper function
	 */
	static <U, R> ImmutableList<R> mapping(U[] elements, Function<? super U, ? extends R> mapper) {
		return new Mapping<>(elements, mapper);
	}

	protected final U[] elements;
	protected ImmutableListImpl(U[] elements) {
		this.elements = elements;
	}

	@Override
	public int size() {
		return elements.length;
	}

	/**
	 * A simple implementation of an immutable List wrapping an Array.
	 * @param <T>
	 */
	static class Simple<T> extends ImmutableListImpl<T, T> {
		Simple(T[] elements) {
			super(elements);
		}

		@Override
		public T get(int index) {
			return super.elements[index];
		}
	}

	/**
	 * An immutable List that execute the given mapping function on get().
	 * @param <T>
	 * @param <U>
	 */
	static class Mapping<T, U> extends ImmutableListImpl<T, U> {
		private final Function<? super U, ? extends T> mapper;

		Mapping(U[] elements, Function<? super U, ? extends T> mapper) {
			super(elements);
			this.mapper = mapper;
		}

		@Override
		public T get(int index) {
			return mapper.apply(elements[index]);
		}
	}
}
