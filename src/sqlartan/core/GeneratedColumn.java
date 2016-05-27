package sqlartan.core;

import java.util.Optional;

/**
 * TODO
 */
public class GeneratedColumn extends Column {
	/**
	 * TODO
	 */
	interface Properties extends Column.Properties {
		Optional<Table> sourceTable();
		Optional<TableColumn> sourceColumn();
	}

	/**
	 * TODO
	 */
	private Properties props;

	/**
	 * TODO
	 *
	 * @param props
	 */
	GeneratedColumn(Properties props) {
		super(props);
		this.props = props;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Optional<Table> sourceTable() { return props.sourceTable(); }

	/**
	 * TODO
	 *
	 * @return
	 */
	public Optional<TableColumn> sourceColumn() {
		return props.sourceColumn();
	}
}
