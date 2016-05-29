package sqlartan.core.alter;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.TypeDefinition;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * TODO
 */
public class AlterTable {
	/**
	 * TODO
	 */
	private Table table;

	/**
	 * Register all actions by grouped by column name
	 */
	private HashMap<String, LinkedList<AlterColumnAction>> columnsActions = new HashMap<>();

	/**
	 * TODO
	 */
	private List<AlterAction> actions = new LinkedList<>();

	/**
	 * TODO
	 *
	 * @param table
	 */
	public AlterTable(Table table) {
		this.table = table;
	}

	/**
	 * TODO
	 */
	public void execute() {
		// execute all registered actions
		for (AlterAction action : actions) {
			try {
				action.execute();
			} catch (SQLException | ParseException e) {
				throw new UncheckedSQLException(e);
			}
			actions.remove(action);
			if (action instanceof AlterColumnAction)
				columnsActions.get(((AlterColumnAction) action).column().name()).remove(action);
		}
	}

	/**
	 * TODO
	 */
	public List<AlterAction> actions() {
		return actions;
	}

	/**
	 * TODO
	 *
	 * @param column
	 */
	public void addColumn(TableColumn column) {
		if (!((!table.column(column.name()).isPresent() && findLastAddColumnAction(column) == null) || findLastDropColumnAction(column) != null))
			throw new UnsupportedOperationException("Column does already exist!");
		try {
			add(column, new AddColumnAction(table, column));
		} catch (ParseException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * TODO
	 *
	 * @param column
	 */
	public void dropColumn(TableColumn column) {
		try {
			if (findLastAddColumnAction(column) != null) {
				add(column, new DropColumnAction(table, findLastAddColumnAction(column).column()));
			} else if (table.column(column.name()).isPresent()) {
				add(column, new DropColumnAction(table, column));
			} else {
				throw new UnsupportedOperationException("Column does not exist!");
			}
		} catch (ParseException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * TODO
	 *
	 * @param columnName
	 * @param column
	 */
	public void modifyColumn(String columnName, TableColumn column) {
		try {
			if ((findLastAddColumnAction(column) != null || table.column(columnName).isPresent()) && findLastDropColumnAction(column) == null) {
				add(column, new ModifyColumnAction(table, column, columnName));
			} else {
				throw new UnsupportedOperationException("Column does not exist!");
			}
		} catch (ParseException e) {
			throw new UncheckedSQLException(e);
		}
	}

	/**
	 * TODO
	 *
	 * @param columns
	 */
	public void setPrimaryKey(List<TableColumn> columns) {
		for (TableColumn column : columns) {
			if (findLastDropColumnAction(column) != null || (findLastAddColumnAction(column) == null && !table.column(column.name()).isPresent()))
				throw new UnsupportedOperationException("Column does not exist!");
		}
		add(new PrimaryKeyAction(table, columns));
	}

	/**
	 * Add action to the stack
	 *
	 * @param column
	 * @param action
	 */
	private void add(TableColumn column, AlterColumnAction action) {
		if (!columnsActions.containsKey(column.name()))
			columnsActions.put(column.name(), new LinkedList<AlterColumnAction>());
		columnsActions.get(column.name()).push(action);
		add(action);
		checkColumnActions(column);
	}

	/**
	 * TODO
	 *
	 * @param action
	 */
	private void add(AlterAction action) {
		if (!actions.contains(action))
			actions.add(action);
	}

	/**
	 * look for unnecessary actions
	 *
	 * @param column
	 */
	private void checkColumnActions(TableColumn column) {
		AlterColumnAction addAction = findLastAddColumnAction(column);
		AlterColumnAction dropAction = findLastDropColumnAction(column);
		if (addAction != null && dropAction != null) {
			if (columnsActions.get(column.name()).indexOf(addAction) < columnsActions.get(column.name()).indexOf(dropAction)) {
				columnsActions.get(column.name()).remove(addAction);
				actions.remove(addAction);
				columnsActions.get(column.name()).remove(dropAction);
				actions.remove(dropAction);
			}
			if (columnsActions.get(column.name()).indexOf(dropAction) < columnsActions.get(column.name()).indexOf(addAction) && table.column(column.name()).isPresent() && compare(dropAction.getColumnDefinition(), addAction.getColumnDefinition())) {
				columnsActions.get(column.name()).remove(addAction);
				actions.remove(addAction);
				columnsActions.get(column.name()).remove(dropAction);
				actions.remove(dropAction);
			}
		}
	}

	/**
	 * Retrieve last add action in the stack
	 *
	 * @param column
	 */
	private AlterColumnAction findLastAddColumnAction(TableColumn column) {
		if (columnsActions.get(column.name()) == null)
			return null;
		AlterColumnAction[] addActions = columnsActions.get(column.name()).stream().filter(action -> action instanceof AddColumnAction).toArray(size -> new AlterColumnAction[size]);
		if (addActions.length == 0)
			return null;
		return addActions[addActions.length - 1];
	}

	/**
	 * Retrieve last drop action in the stack
	 *
	 * @param column
	 */
	private AlterColumnAction findLastDropColumnAction(TableColumn column) {
		if (columnsActions.get(column.name()) == null)
			return null;
		AlterColumnAction[] dropActions = columnsActions.get(column.name()).stream().filter(action -> action instanceof DropColumnAction).toArray(size -> new AlterColumnAction[size]);
		if (dropActions.length == 0)
			return null;
		return dropActions[dropActions.length - 1];
	}

	/**
	 * Compare two columns definitions, with name, type, ...
	 *
	 * @param col1
	 * @param col2
	 */
	private boolean compare(ColumnDefinition col1, ColumnDefinition col2) {
		if (!col1.name.equals(col2.name))
			return false;

		if ((!col1.type.isPresent() && col2.type.isPresent()) || (col1.type.isPresent() && !col2.type.isPresent()))
			return false;

		if (col1.type.isPresent()) {
			TypeDefinition type1 = col1.type.get();
			TypeDefinition type2 = col2.type.get();

			if (!type1.name.equals(type2.name))
				return false;

			if ((!type1.length.isPresent() && type2.length.isPresent()) || (type1.length.isPresent() && !type2.length.isPresent()))
				return false;

			if (type1.length.isPresent()) {
				if (!type1.length.get().sign.equals(type2.length.get().sign) || !type1.length.get().value.equals(type2.length.get().value))
					return false;
			}

			if ((!type1.scale.isPresent() && type2.scale.isPresent()) || (type1.scale.isPresent() && !type2.scale.isPresent()))
				return false;

			if (type1.scale.isPresent()) {
				if (!type1.scale.get().sign.equals(type2.scale.get().sign) || !type1.scale.get().value.equals(type2.scale.get().value))
					return false;
			}
		}

		if ((!col1.constraints.isEmpty() && col2.constraints.isEmpty()) || (col1.constraints.isEmpty() && !col2.constraints.isEmpty()) || col1.constraints.size() != col2.constraints.size())
			return false;

		if (!col1.constraints.isEmpty()) {
			int n = col1.constraints.size();
			for (int i = 0; i < n; i++) {
				if ((!col1.constraints.get(i).name.isPresent() && col2.constraints.get(i).name.isPresent()) || (col1.constraints.get(i).name.isPresent() && !col2.constraints.get(i).name.isPresent()))
					return false;

				if (col1.constraints.get(i).name.isPresent() && !col1.constraints.get(i).name.get().equals(col2.constraints.get(i).name.get()))
					return false;
			}
		}

		return true;
	}
}

