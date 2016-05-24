package sqlartan.core.alterTable;

import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.ColumnDefinition;
import sqlartan.core.ast.TypeDefinition;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by matthieu.villard on 01.05.2016.
 */
public class AlterTable
{
	private Table table;

	// register all actions by grouped by column name
	private HashMap<String, LinkedList<AlterColumnAction>> columnsActions = new HashMap<>();

	private List<AlterAction> actions = new ArrayList<>();

	public AlterTable(Table table) {
		this.table = table;
	}

	public void execute() throws ParseException {
		// execute all registred actions
		for(AlterAction action : actions){
			try {
				action.execute();
			} catch (SQLException e) {
				throw new UncheckedSQLException(e);
			}
			actions.remove(action);
			if(action instanceof AlterColumnAction)
				columnsActions.get(((AlterColumnAction) action).column().name()).remove(action);
		}
	}

	public List<AlterAction> actions(){
		return actions;
	}

	public void addColumn(TableColumn column) throws ParseException, SQLException {
		if(!((!table.column(column.name()).isPresent() && findLastAddColumnAction(column) == null) || findLastDropColumnAction(column) != null))
			throw new UnsupportedOperationException("Column does already exist!");
		add(column, new AddColumnAction(table, column));
	}

	public void dropColumn(TableColumn column) throws ParseException, SQLException {
		if(findLastAddColumnAction(column) != null) {
			add(column, new DropColumnAction(table, findLastAddColumnAction(column).column()));
		}
		else if(table.column(column.name()).isPresent()) {
			add(column, new DropColumnAction(table, column));
		}
		else {
			throw new UnsupportedOperationException("Column does not exist!");
		}
	}

	public void modifyColumn(String columnName, TableColumn column) throws ParseException, SQLException {
		if((findLastAddColumnAction(column) != null || table.column(columnName).isPresent()) && findLastDropColumnAction(column) == null) {
			add(column, new ModifyColumnAction(table, column, columnName));
		}
		else{
			throw new UnsupportedOperationException("Column does not exist!");
		}
	}

	public void setPrimaryKey(List<TableColumn> columns){
		for(TableColumn column : columns){
			if(findLastDropColumnAction(column) != null || (findLastAddColumnAction(column) == null && !table.column(column.name()).isPresent()))
				throw new UnsupportedOperationException("Column does not exist!");
		}
		add(new PrimaryKeyAction(table, columns));
	}

	// Add action to the stack
	private void add(TableColumn column, AlterColumnAction action) throws ParseException, SQLException {
		if(!columnsActions.containsKey(column.name()))
			columnsActions.put(column.name(), new LinkedList<AlterColumnAction>());
		columnsActions.get(column.name()).push(action);
		add(action);
		checkColumnActions(column);
	}

	private void add(AlterAction action) {
		if(!actions.contains(action))
			actions.add(action);
	}

	// look for unnecessary actions
	private void checkColumnActions(TableColumn column) throws ParseException, SQLException {
		AlterColumnAction addAction = findLastAddColumnAction(column);
		AlterColumnAction dropAction = findLastDropColumnAction(column);
		if(addAction != null && dropAction != null) {
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

	// retrieve last add action in the stack
	private AlterColumnAction findLastAddColumnAction(TableColumn column){
		if(columnsActions.get(column.name()) == null)
			return null;
		AlterColumnAction[] addActions = columnsActions.get(column.name()).stream().filter(action -> action instanceof AddColumnAction).toArray(size -> new AlterColumnAction[size]);
		if(addActions.length == 0)
			return null;
		return addActions[addActions.length - 1];
	}

	// retrieve last drop action in the stack
	private AlterColumnAction findLastDropColumnAction(TableColumn column){
		if(columnsActions.get(column.name()) == null)
			return null;
		AlterColumnAction[] dropActions = columnsActions.get(column.name()).stream().filter(action -> action instanceof DropColumnAction).toArray(size -> new AlterColumnAction[size]);
		if(dropActions.length == 0)
			return null;
		return dropActions[dropActions.length - 1];
	}

	// compare two columns definitions, with name, type, ...
	private boolean compare(ColumnDefinition col1, ColumnDefinition col2){
		if(!col1.name.equals(col2.name))
			return false;

		if((!col1.type.isPresent() && col2.type.isPresent()) || (col1.type.isPresent() && !col2.type.isPresent()))
			return false;

		if(col1.type.isPresent()){
			TypeDefinition type1 = col1.type.get();
			TypeDefinition type2 = col2.type.get();

			if(!type1.name.equals(type2.name))
				return false;

			if((!type1.length.isPresent() && type2.length.isPresent()) || (type1.length.isPresent() && !type2.length.isPresent()))
				return false;

			if(type1.length.isPresent()){
				if(!type1.length.get().sign.equals(type2.length.get().sign) || !type1.length.get().value.equals(type2.length.get().value))
					return false;
			}

			if((!type1.scale.isPresent() && type2.scale.isPresent()) || (type1.scale.isPresent() && !type2.scale.isPresent()))
				return false;

			if(type1.scale.isPresent()){
				if(!type1.scale.get().sign.equals(type2.scale.get().sign) || !type1.scale.get().value.equals(type2.scale.get().value))
					return false;
			}
		}

		if((!col1.constraints.isEmpty() && col2.constraints.isEmpty()) || (col1.constraints.isEmpty() && !col2.constraints.isEmpty()) || col1.constraints.size() != col2.constraints.size())
			return false;

		if(!col1.constraints.isEmpty()){
			int n = col1.constraints.size();
			for(int i = 0; i < n; i++){
				if((!col1.constraints.get(i).name.isPresent() && col2.constraints.get(i).name.isPresent()) || (col1.constraints.get(i).name.isPresent() && !col2.constraints.get(i).name.isPresent()))
					return false;

				if(col1.constraints.get(i).name.isPresent() && !col1.constraints.get(i).name.get().equals(col2.constraints.get(i).name.get()))
					return false;
			}
		}

		return true;
	}
}

