package sqlartan.core;

import sqlartan.core.alter.AlterTable;
import java.util.Objects;
import java.util.Optional;

/**
 * Defines the column of a table
 */
public class TableColumn extends Column {
	/**
	 * TODO
	 */
	public interface Properties extends Column.Properties {
		boolean unique();
		boolean primaryKey();
		String check();
	}

	/**
	 * TODO
	 */
	private Table parent;
	/**
	 * TODO
	 */
	private Properties props;

	/**
	 * TODO
	 *
	 * @param table
	 * @param props
	 */
	public TableColumn(Table table, Properties props) {
		super(props);
		this.parent = table;
		this.props = props;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Table parentTable() {
		return parent;
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean unique() {
		return props.unique();
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public Optional<String> check() {
		return Optional.ofNullable(props.check());
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	public boolean primaryKey() {
		return props.primaryKey();
	}

	/**
	 * Rename the column.
	 *
	 * @param name
	 */
	public void rename(String name) {
		boolean unique = props.unique();
		String check = props.check();
		String type = props.type();
		boolean nullable = props.nullable();
		boolean pk = props.primaryKey();

		TableColumn column = new TableColumn(parentTable(), new Properties() {
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
		AlterTable alter = parentTable().alter();
		alter.modifyColumn(name(), column);
		alter.execute();
	}

	/**
	 * Drop the column.
	 */
	public void drop() {
		AlterTable alter = parentTable().alter();
		alter.dropColumn(this);
		alter.execute();
	}

	/**
	 * TODO
	 *
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof TableColumn) {
			TableColumn col = (TableColumn) obj;
			return parentTable().equals(col.parentTable()) && name().equals(col.name());
		} else {
			return false;
		}
	}

	/**
	 * TODO
	 *
	 * @return
	 */
	@Override
	public int hashCode() {
		return Objects.hash(parent, name());
	}
}
