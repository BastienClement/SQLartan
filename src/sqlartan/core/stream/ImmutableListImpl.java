package sqlartan.core.stream;

import java.util.AbstractList;
import java.util.function.Function;

/**
 * Concrete implementation of the ImmutableList interface.
 * This implementation uses a simple object array as storage.
 *
 * @param <T> the type of elements in the list
 */
class ImmutableListImpl<T> extends AbstractList<T> implements ImmutableList<T> {
	/**
	 * Constructs a new list from an array of elements.
	 * The array will be cloned to ensure immutability.
	 *
	 * @param elements an array of elements to clone
	 * @param <U>      the type of elements in the array
	 */
	@SuppressWarnings("unchecked")
	static <U> ImmutableListImpl<U> from(U[] elements) {
		Object[] clone = new Object[elements.length];
		System.arraycopy(elements, 0, clone, 0, elements.length);
		return new ImmutableListImpl<>((U[]) clone);
	}

	/**
	 * Constructs a new list from an array of elements.
	 * The array will be used as-is. Care must be taken to ensure it is effectively immutable.
	 *
	 * @param elements an immutable array of elements
	 * @param <U>      the type of elements in the array
	 */
	static <U> ImmutableListImpl<U> wrap(U[] elements) {
		return new ImmutableListImpl<>(elements);
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
	static <U, R> ImmutableListImpl<R> mapping(U[] elements, Function<? super U, ? extends R> mapper) {
		return new ImmutableListImpl<R>(null) {
			@Override
			public R get(int index) {
				return mapper.apply(elements[index]);
			}

			@Override
			public int size() {
				return elements.length;
			}
		};
	}

	private T[] elements;

	private ImmutableListImpl(T[] elements) {
		this.elements = elements;
	}

	@Override
	public T get(int index) {
		return elements[index];
	}

	@Override
	public int size() {
		return elements.length;
	}
}
