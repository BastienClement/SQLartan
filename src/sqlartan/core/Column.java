package sqlartan.core;

/**
 * A column from a structure.
 */
public abstract class Column {
	/**
	 * Defines the properties of a column.
	 * A column has a name, a type and can be nullable or not.
	 */
	interface Properties {
		String name();
		String type();
		boolean nullable();
	}

	/**
	 * Properties of the column
	 */
	private Properties props;

	/**
	 * @param props the properties of the column
	 */
	protected Column(Properties props) {
		this.props = props;
	}

	/**
	 * Returns the name of the column.
	 *
	 * @return the name of the column
	 */
	public String name() {
		return props.name();
	}

	/**
	 * Returns the type of the column.
	 *
	 * @return the type of the column
	 */
	public String type() {
		return props.type();
	}

	/**
	 * Returns the affinity associated with the type of the column.
	 *
	 * @return the affinity associated with the type of the column
	 */
	public Affinity affinity() {
		return Affinity.forType(props.type());
	}

	/**
	 * Return whether this column is nullable or not.
	 *
	 * @return true if the column is nullable, false otherwise
	 */
	public boolean nullable() {
		return props.nullable();
	}

}
