package sqlartan.core;

import java.util.List;
import java.util.Optional;

/**
 * A Structure is a database object that possess a table-like shape
 * with columns and rows.
 *
 * @param <T> the actual type of the columns of this structure
 */
public interface Structure<T extends Column> {
	/**
	 * Returns the list of columns composing this structure.
	 */
	List<T> columns();

	/**
	 * Returns the number number of columns composing this structure.
	 */
	int columnCount();

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
