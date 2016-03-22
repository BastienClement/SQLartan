package sqlartan.core;

import java.util.Optional;

public class TableColumn extends Column {
	public static interface Properties extends Column.Properties {
		boolean unique();
		String check();
	}

	private Table parent;
	private Properties props;

	TableColumn(Table table, Properties props) {
		super(props);
		this.parent = table;
		this.props = props;
	}

	public Table parentTable() {
		return parent;
	}

	public boolean unique() {
		return props.unique();
	}

	public Optional<String> check() {
		return Optional.ofNullable(props.check());
	}
}
