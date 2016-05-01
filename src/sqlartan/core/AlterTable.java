package sqlartan.core;

import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Created by matthieu.villard on 01.05.2016.
 */
public class AlterTable
{
	private final Table table;

	// register all actions by grouped by column name
	private HashMap<String, LinkedList<AlterAction>> actions = new HashMap<>();

	public AlterTable(Table table){
		this.table = table;
	}

	public void execute(){
		// execute all registred actions
		Iterator<String> keySetIterator = actions.keySet().iterator();
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			for(AlterAction action : actions.get(key)){
				try {
					action.execute();
				} catch (SQLException e) {
					throw new RuntimeSQLException(e);
				}
			}
		}
	}

	public void addColumn(String columnName, String typeName){
		if(!((!table.column(columnName).isPresent() && findLastAddAction(columnName) == null) || findLastDropAction(columnName) != null))
			throw new UnsupportedOperationException("Column does already exist!");
		add(columnName, new AlterAction(new TableColumn(table, new TableColumn.Properties() {
				@Override
				public boolean unique() {
					return false;
				}
				@Override
				public String check() {
					return null;
				}
				@Override
				public String name() {
					return columnName;
				}
				@Override
				public String type() {
					return typeName;
				}
				@Override
				public boolean nullable() {
					return false;
				}
			}), ActionType.ADD)
		);
	}

	public void dropColumn(String columnName){
		if(findLastAddAction(columnName) != null)
			add(columnName, new AlterAction(findLastAddAction(columnName).column(), ActionType.DROP));
		else if(table.column(columnName).isPresent())
			add(columnName, new AlterAction(table.column(columnName).get(), ActionType.DROP));
		else
			throw new UnsupportedOperationException("Column does not exist!");
	}

	public void modifyColumn(String columnName, String newName, String typeName, boolean unique, boolean nullable){
		if(findLastAddAction(columnName) != null || table.column(columnName).isPresent()) {
			add(columnName, new AlterAction(new TableColumn(table, new TableColumn.Properties() {
						@Override
						public boolean unique() {
							return unique;
						}
						@Override
						public String check() {
							return null;
						}
						@Override
						public String name() {
							return newName;
						}
						@Override
						public String type() {
							return typeName;
						}
						@Override
						public boolean nullable() {
							return nullable;
						}
					}), findLastAddAction(columnName) != null ? findLastAddAction(columnName).column() : table.column(columnName).get(),
					ActionType.MODIFY)
			);
		}
	}

	public void modifyColumn(String columnName, String newName, String typeName){
		if(findLastAddAction(columnName) != null || table.column(columnName).isPresent()) {
			TableColumn column = (findLastAddAction(columnName) != null ? findLastAddAction(columnName).column() : table.column(columnName).get());
			modifyColumn(columnName, newName, typeName, column.unique(), column.nullable());
		}
	}

	public void modifyColumn(String columnName, String newName){
		if(findLastAddAction(columnName) != null || table.column(columnName).isPresent()) {
			TableColumn column = (findLastAddAction(columnName) != null ? findLastAddAction(columnName).column() : table.column(columnName).get());
			modifyColumn(columnName, newName, column.type(), column.unique(), column.nullable());
		}
	}

	// Add action to the stack
	private void add(String columnName, AlterAction action){
		if(!actions.containsKey(columnName))
			actions.put(columnName, new LinkedList<AlterAction>());
		actions.get(columnName).add(action);
		check(columnName);
	}

	// look for unnecessary actions
	private void check(String columnName){
		AlterAction addAction = findLastAddAction(columnName);
		AlterAction dropAction = findLastDropAction(columnName);
		if(addAction != null && dropAction != null) {
			if (actions.get(columnName).indexOf(addAction) < actions.get(columnName).indexOf(dropAction)) {
				actions.get(columnName).remove(addAction);
				actions.get(columnName).remove(dropAction);
			}
			if (actions.get(columnName).indexOf(dropAction) < actions.get(columnName).indexOf(addAction) && table.column(columnName).isPresent() && compare(table.column(columnName).get(), addAction.column())) {
				actions.get(columnName).remove(addAction);
				actions.get(columnName).remove(dropAction);
			}
		}
	}

	// retrieve last add action in the stack
	private AlterAction findLastAddAction(String columnName){
		if(actions.get(columnName) == null)
			return null;
		AlterAction[] addActions = actions.get(columnName).stream().filter(action -> action.type() == ActionType.ADD).toArray(size -> new AlterAction[size]);
		if(addActions.length == 0)
			return null;
		return addActions[addActions.length - 1];
	}

	// retrieve last drop action in the stack
	private AlterAction findLastDropAction(String columnName){
		if(actions.get(columnName) == null)
			return null;
		AlterAction[] dropActions = actions.get(columnName).stream().filter(action -> action.type() == ActionType.DROP).toArray(size -> new AlterAction[size]);
		if(dropActions.length == 0)
			return null;
		return dropActions[dropActions.length - 1];
	}

	// compare two columns, with name, type, ...
	private boolean compare(TableColumn col1, TableColumn col2){
		return col1.name().equals(col2.name()) && col1.type().equals(col2.name()) && col1.unique() == col2.unique() && col1.nullable() == col2.nullable();
	}

	// allowed actions, maybe to improve
	private enum ActionType {
		ADD,
		DROP,
		MODIFY
	}

	private class AlterAction
	{
		private final TableColumn originalColumn;
		private final TableColumn column;
		private ActionType type;

		AlterAction(TableColumn column, ActionType type){
			originalColumn = null;
			this.column = column;
			this.type = type;
		}

		AlterAction(TableColumn column, TableColumn originalColumn, ActionType type){
			this.originalColumn = originalColumn;
			this.column = column;
			this.type = type;
		}

		public void execute() throws SQLException {
			// execute, depending of action type, to improve
			if(type == ActionType.DROP){
				table.database.assemble(
						"CREATE TABLE ", table.database.name(), ".", table.name() + "_backup", "(" +
						table.columns().filter(col -> !col.name().equals(column.name())).map(col -> col.name() + " " + col.type()  + (col.unique() ? " UNIQUE" : "") + (col.nullable() ? "" : " NOT NULL")).collect(Collectors.joining(", ")) +
						", PRIMARY KEY(" + table.primaryKey().getColumns().stream().collect(Collectors.joining(", ")) +
						"))"
				).execute();

				table.database.assemble(
						"INSERT INTO ", table.database.name(), ".", table.name() + "_backup", "SELECT " +
								table.columns().filter(col -> !col.name().equals(column.name())).map(col -> col.name()).collect(Collectors.joining(", ")) +
								" FROM " + table.fullName()
				).execute();
				table.drop();
				table.database.table(table.name() + "_backup").get().rename(table.name());
			}
			else if(type == ActionType.ADD){
				String query = "ALTER TABLE " + column.parentTable().fullName() + "  ADD COLUMN " + column.name() + " " + column.type();
				table.database.execute(query);
			}
			else{
				table.database.assemble(
						"CREATE TABLE ", table.database.name(), ".", table.name() + "_backup", "(" +
								table.columns().filter(col -> !col.name().equals(originalColumn.name())).map(col -> col.name() + " " + col.type()  + (col.unique() ? " UNIQUE" : "") + (col.nullable() ? "" : " NOT NULL")).collect(Collectors.joining(", ")) +
								", " + column.name() + " " + column.type()  + (column.unique() ? " UNIQUE" : "") + (column.nullable() ? "" : " NOT NULL") +
								", PRIMARY KEY(" + table.primaryKey().getColumns().stream().collect(Collectors.joining(", ")) +
								"))"
				).execute();

				table.database.assemble(
						"INSERT INTO ", table.database.name(), ".", table.name() + "_backup", "SELECT " +
								table.columns().filter(col -> !col.name().equals(originalColumn.name())).map(col -> col.name()).collect(Collectors.joining(", ")) +
								", " + originalColumn.name() +
								" FROM " + table.fullName()
				).execute();

				table.drop();
				table.database.table(table.name() + "_backup").get().rename(table.name());
			}
		}

		public TableColumn column(){
			return column;
		}

		public ActionType type(){
			return type;
		}
	}

}

