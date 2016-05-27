package sqlartan.core;

/**
 * Defines a column
 */
public abstract class Column {
	/**
	 * Defines the properties of a column.
	 * A column has a name, a type and can be null or not.
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
	 * Constructs a column with the given properties.
	 *
	 * @param props
	 */
	protected Column(Properties props) {
		this.props = props;
	}

	/**
	 * Get the name of a column.
	 *
	 * @return the name
	 */
	public String name() {
		return props.name();
	}

	/**
	 * Get the type of the column.
	 *
	 * @return the type
	 */
	public String type() {
		return props.type();
	}

	/**
	 * Get the affinity of the column.
	 *
	 * @return the affinity
	 */
	public Affinity affinity() {
		return Affinity.forType(props.type());
	}

	/**
	 * Return true if the column is nullable or false.
	 *
	 * @return the property nullable
	 */
	public boolean nullable() {
		return props.nullable();
	}

}
