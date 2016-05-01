package sqlartan.core;

import sqlartan.core.util.RuntimeSQLException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by matthieu.villard on 01.05.2016.
 */
public class AlterTable
{
	private final Table table;
	private HashMap<String, LinkedList<AlterAction>> actions = new HashMap<>();

	public AlterTable(Table table){
		this.table = table;
	}

	public void execute(){
		Iterator<String> keySetIterator = actions.keySet().iterator();
		while(keySetIterator.hasNext()){
			String key = keySetIterator.next();
			for(AlterAction action : actions.get(key)){
				try {
					table.database.execute(action.query());
				} catch (SQLException e) {
					throw new RuntimeSQLException(e);
				}
			}
		}
	}

	public void addColumn(String columnName, String typeName){
		if(!(!table.column(columnName).isPresent() && findLastAddAction(columnName) == null || findLastDropAction(columnName) != null))
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

	public void drop(String columnName){
		if(table.column(columnName).isPresent())
			add(columnName, new AlterAction(table.column(columnName).get(), ActionType.DROP));
		else if(findLastAddAction(columnName) != null)
			add(columnName, new AlterAction(findLastAddAction(columnName).column(), ActionType.DROP));
		else
			throw new UnsupportedOperationException("Column does not exist!");
	}

	public LinkedList<AlterAction> operations(String columnName){
		return actions.get(columnName);
	}

	private void add(String columnName, AlterAction action){
		if(!actions.containsKey(columnName))
			actions.put(columnName, new LinkedList<AlterAction>());
		actions.get(columnName).add(action);
		check(columnName);
	}

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

	private AlterAction findLastAddAction(String columnName){
		if(actions.get(columnName) == null)
			return null;
		AlterAction[] addActions = actions.get(columnName).stream().filter(action -> action.type() == ActionType.ADD).toArray(size -> new AlterAction[size]);
		if(addActions.length == 0)
			return null;
		return addActions[addActions.length - 1];
	}

	private AlterAction findLastDropAction(String columnName){
		if(actions.get(columnName) == null)
			return null;
		AlterAction[] dropActions = actions.get(columnName).stream().filter(action -> action.type() == ActionType.DROP).toArray(size -> new AlterAction[size]);
		if(dropActions.length == 0)
			return null;
		return dropActions[dropActions.length - 1];
	}

	private boolean compare(TableColumn col1, TableColumn col2){
		return col1.name().equals(col2.name()) && col1.type().equals(col2.name()) && col1.unique() == col2.unique() && col1.nullable() == col2.nullable();
	}

	private enum ActionType {
		ADD,
		DROP,
		MODIFY
	}

	private class AlterAction
	{
		private final TableColumn column;
		private ActionType type;

		AlterAction(TableColumn column, ActionType type){
			this.column = column;
			this.type = type;
		}

		public String query(){
			String query = "ALTER TABLE " + column.parentTable().fullName();
			if(type == ActionType.ADD){
				query += "  ADD COLUMN " + column.name() + " " + column.type();
			}
			return query;
		}

		public TableColumn column(){
			return column;
		}

		public ActionType type(){
			return type;
		}
	}

}

