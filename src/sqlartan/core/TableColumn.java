package sqlartan.core;

import sqlartan.core.alter.AlterTable;
import java.util.Optional;

/**
 * A column of a table
 */
public class TableColumn extends Column {
	/**
	 * The properties of a table column.
	 * In addition to the properties of a Column, a table column can be
	 * unique, primaryKey and have a check constraint.
	 */
	public interface Properties extends Column.Properties {
		boolean unique();
		boolean primaryKey();
		String check();
	}

	/**
	 * The parent table
	 */
	private Table parent;

	/**
	 * The column properties
	 */
	private Properties props;

	/**
	 * @param table the parent table
	 * @param props the column properties
	 */
	public TableColumn(Table table, Properties props) {
		super(props);
		this.parent = table;
		this.props = props;
	}

	/**
	 * Returns the parent table.
	 *
	 * @return the parent table
	 */
	public Table table() {
		return parent;
	}

	/**
	 * Checks whether this column is part of a PRIMARY KEY or UNIQUE index.
	 * <p>
	 * All columns that are parts of a multiple-columns index are considered
	 * unique. When manipulating such index, care should be taken to ensure
	 * that every column of the index are present since a single column, while
	 * being consider unique by this method, is not necessary unique by itself.
	 *
	 * @return true if this column is unique, false otherwise
	 */
	public boolean unique() {
		return props.unique();
	}

	/**
	 * Returns the check constraint expression of this column.
	 *
	 * @return the check constraint
	 */
	public Optional<String> check() {
		return Optional.ofNullable(props.check());
	}

	/**
	 * Checks whether this column is a part of the table PRIMARY KEY index.
	 *
	 * @return true if the column is part of the primary key
	 */
	public boolean primaryKey() {
		return props.primaryKey();
	}

	/**
	 * Rename the column.
	 *
	 * @param name the new name of the column
	 */
	public void rename(String name) {
		boolean unique = props.unique();
		String check = props.check();
		String type = props.type();
		boolean nullable = props.nullable();
		boolean pk = props.primaryKey();

		TableColumn column = new TableColumn(table(), new Properties() {
			@Override
			public boolean unique() {
				return unique;
			}
			@Override
			public boolean primaryKey() {
				return pk;
			}
			@Override
			public String check() {
				return check;
			}
			@Override
			public String name() {
				return name;
			}
			@Override
			public String type() {
				return type;
			}
			@Override
			public boolean nullable() {
				return nullable;
			}
		});
		AlterTable alter = table().alter();
		alter.modifyColumn(name(), column);
		alter.execute();
	}

	/**
	 * Drop the column.
	 */
	public void drop() {
		AlterTable alter = table().alter();
		alter.dropColumn(this);
		alter.execute();
	}
}
