package sqlartan.core;

import sqlartan.core.stream.IterableStream;
import java.util.Optional;

/**
 * A TabStructure is a database object that possess a table-like shape
 * with columns and rows.
 *
 * @param <T> the actual type of the columns of this structure
 */
public interface Structure<T extends Column> {
	/**
	 * Returns the list of columns composing this structure.
	 */
	IterableStream<T> columns();

	/**
	 * Returns the columns with the given name, if it exists.
	 *
	 * @param name the name of the column
	 */
	Optional<T> column(String name);

	/**
	 * Returns the i-th column from this structure, it it exists
	 *
	 * @param idx the index of the column
	 */
	Optional<T> column(int idx);
}
