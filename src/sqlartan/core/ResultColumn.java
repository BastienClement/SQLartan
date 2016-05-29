package sqlartan.core;

/**
 * A column of a result set.
 * <p>
 * In addition to a generic GeneratedColumn, this kind of column possess an
 * index in the result set columns list. This index is required to distinguish
 * result columns with the same name.
 */
public class ResultColumn extends GeneratedColumn {
	/**
	 * The parent result set
	 */
	private Result result;

	/**
	 * This column index in the result set
	 */
	private int index;

	/**
	 * @param result the parent result set
	 * @param index  the index of the column
	 * @param props  the properties of the column
	 */
	ResultColumn(Result result, int index, Properties props) {
		super(props);
		this.result = result;
		this.index = index;
	}

	/**
	 * Returns the parent result set.
	 *
	 * @return the parent result set
	 */
	public Result result() {
		return result;
	}

	/**
	 * Returns the index of this column in the result set
	 *
	 * @return the index of this column
	 */
	public int index() {
		return index;
	}
}
