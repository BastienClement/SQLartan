package sqlartan.core;

import sqlartan.core.alterTable.AlterTable;
import sqlartan.core.ast.parser.ParseException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Defines a column of a table
 */
public class TableColumn extends Column {
	public interface Properties extends Column.Properties {
		boolean unique();
		String check();
	}

	private Table parent;
	private Properties props;

	public TableColumn(Table table, Properties props) {
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
	public void rename(String name) throws ParseException, SQLException {
		props = new Properties() {
			@Override
			public boolean unique() {
				return unique();
			}
			@Override
			public String check() {
				return check();
			}
			@Override
			public String name() {
				return name;
			}
			@Override
			public String type() {
				return type();
			}
			@Override
			public boolean nullable() {
				return nullable();
			}
		};

		AlterTable alter = parentTable().alter();
		alter.modifyColumn(name(), this);
		alter.execute();
	}

	/**
	 * Drop the column
	 */
	public void drop() throws ParseException, SQLException {
		AlterTable alter = parentTable().alter();
		alter.dropColumn(this);
		alter.execute();
	}
}
