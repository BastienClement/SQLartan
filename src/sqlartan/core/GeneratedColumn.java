package sqlartan.core;

import java.util.Optional;

public class GeneratedColumn extends Column {
	interface Properties extends Column.Properties {
		Optional<Table> sourceTable();
		Optional<TableColumn> sourceColumn();
	}

	private Properties props;

	GeneratedColumn(Properties props) {
		super(props);
		this.props = props;
	}

	public Optional<Table> sourceTable() { return props.sourceTable(); }
	public Optional<TableColumn> sourceColumn() {
		return props.sourceColumn();
	}
}
