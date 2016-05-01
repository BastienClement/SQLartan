package sqlartan.core;

import java.util.Optional;

/**
 * Defines a column of a table
 */
public class TableColumn extends Column {
	interface Properties extends Column.Properties {
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

	/**
	 * Rename the column
	 *
	 * @param name
	 */
	public void rename(String name) {
		parentTable().alter().modifyColumn(name(), name);
	}

	/**
	 * Drop the column
	 */
	public void drop() {
		parentTable().alter().dropColumn(name());
	}
}
