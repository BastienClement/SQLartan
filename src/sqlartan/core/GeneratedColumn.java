package sqlartan.core;

import java.util.Optional;

/**
 * A structure column generated from query.
 */
public class GeneratedColumn extends Column {
	/**
	 * Generated column properties.
	 * In addition to the properties of a generic column, this interface
	 * also defines an optional source table and source column.
	 */
	interface Properties extends Column.Properties {
		Optional<Table> sourceTable();
		Optional<TableColumn> sourceColumn();
	}

	/**
	 * The properties of this column
	 */
	private Properties props;

	/**
	 * @param props the properties of the column
	 */
	GeneratedColumn(Properties props) {
		super(props);
		this.props = props;
	}

	/**
	 * Returns the source table if it is known.
	 *
	 * @return the source table if it is known.
	 */
	public Optional<Table> sourceTable() { return props.sourceTable(); }

	/**
	 * Returns the source column from which this column was generated,
	 * if it is known
	 *
	 * @return the source column of this one, if it is known
	 */
	public Optional<TableColumn> sourceColumn() {
		return props.sourceColumn();
	}
}
