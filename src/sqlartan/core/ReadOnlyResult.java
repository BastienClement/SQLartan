package sqlartan.core;

import sqlartan.core.stream.ImmutableList;
import java.util.Optional;

/**
 * TODO
 */
public interface ReadOnlyResult extends Structure<ResultColumn> {
	/**
	 * TODO
	 *
	 * @return
	 */
	String query();

	/**
	 * TODO
	 *
	 * @return
	 */
	boolean isQueryResult();

	/**
	 * TODO
	 *
	 * @return
	 */
	boolean isUpdateResult();

	/**
	 * TODO
	 *
	 * @return
	 */
	int updateCount();

	/**
	 * TODO
	 *
	 * @return
	 */
	ImmutableList<ResultColumn> columns();

	/**
	 * TODO
	 *
	 * @param name the name of the column
	 * @return
	 */
	Optional<ResultColumn> column(String name);

	/**
	 * TODO
	 *
	 * @param idx the index of the column
	 * @return
	 */
	Optional<ResultColumn> column(int idx);
}
