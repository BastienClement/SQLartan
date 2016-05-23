package sqlartan.core.alterTable;

import javafx.scene.control.Tab;
import sqlartan.core.Index;
import sqlartan.core.Row;
import sqlartan.core.Table;
import sqlartan.core.TableColumn;
import sqlartan.core.ast.*;
import sqlartan.core.ast.parser.ParseException;
import sqlartan.core.ast.parser.Parser;
import sqlartan.core.ast.parser.ParserContext;
import sqlartan.core.ast.token.TokenSource;
import sqlartan.core.ast.token.TokenizeException;
import sqlartan.core.stream.IterableStream;
import sqlartan.core.util.UncheckedSQLException;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by matthieu.villard on 01.05.2016.
 */
public class AlterTable
{
	private final Table table;

	// register all actions by grouped by column name
	private HashMap<TableColumn, LinkedList<AlterColumnAction>> columnsActions = new HashMap<>();

	private List<AlterAction> actions = new LinkedList();

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
				columnsActions.remove(((AlterColumnAction) action).column.name);
		}
	}

	public List<AlterAction> actions(){
		return actions;
	}

	public void addColumn(TableColumn column) throws ParseException, SQLException {
		if(!((!table.column(column.name()).isPresent() && findLastAddColumnAction(column.name()) == null) || findLastDropColumnAction(column.name()) != null))
			throw new UnsupportedOperationException("Column does already exist!");
		add(column, new AddColumnAction(column.getDefinition()));
	}

	public void dropColumn(TableColumn column) throws ParseException, SQLException {
		if(findLastAddColumnAction(column) != null) {
			add(column, new DropColumnAction(findLastAddColumnAction(column).column()));
		}
		else if(table.column(column.name()).isPresent())
			add(column, new DropColumnAction(column.getDefinition()));
		else
			throw new UnsupportedOperationException("Column does not exist!");
	}

	public void modifyColumn(String columnName, TableColumn column) throws ParseException, SQLException {
		if((findLastAddColumnAction(column) != null || table.column(column.name()).isPresent()) && findLastDropColumnAction(column) == null) {
			add(columnName, new ModifyColumnAction(column.getDefinition(), columnName));
		}
	}

	public void setPrimaryKey(String[] columnsNames){
		for(String columnName : columnsNames){
			if(findLastDropColumnAction(columnName) != null || (findLastAddColumnAction(columnName) == null && !table.column(columnName).isPresent()))
				throw new UnsupportedOperationException("Column does not exist!");
		}
	}

	// Add action to the stack
	private void add(TableColumn column, AlterColumnAction action) throws ParseException, SQLException {
		if(!columnsActions.containsKey(column))
			columnsActions.put(column, new LinkedList<AlterColumnAction>());
		columnsActions.get(column).push(action);
		add(action);
		checkColumnActions(column);
	}

	private void add(AlterAction action) throws ParseException, SQLException {
		if(!actions.contains(action))
			actions.add(action);
	}

	// look for unnecessary actions
	private void checkColumnActions(TableColumn column) throws ParseException, SQLException {
		AlterColumnAction addAction = findLastAddColumnAction(column);
		AlterColumnAction dropAction = findLastDropColumnAction(column);
		if(addAction != null && dropAction != null) {
			if (columnsActions.get(column).indexOf(addAction) < columnsActions.get(column).indexOf(dropAction)) {
				columnsActions.get(column).remove(addAction);
				actions.remove(addAction);
				columnsActions.get(column).remove(dropAction);
				actions.remove(dropAction);
			}
			if (columnsActions.get(column).indexOf(dropAction) < columnsActions.get(column).indexOf(addAction) && table.column(column.name()).isPresent() && compare(getTableDefinition().columns.stream().filter(col -> col.name.equals(column.name())).findFirst().get(), addAction.column())) {
				columnsActions.get(column).remove(addAction);
				actions.remove(addAction);
				columnsActions.get(column).remove(dropAction);
				actions.remove(dropAction);
			}
		}
	}

	// retrieve last add action in the stack
	private AlterColumnAction findLastAddColumnAction(TableColumn column){
		if(columnsActions.get(column) == null)
			return null;
		AlterColumnAction[] addActions = columnsActions.get(column).stream().filter(action -> action instanceof AddColumnAction).toArray(size -> new AlterColumnAction[size]);
		if(addActions.length == 0)
			return null;
		return addActions[addActions.length - 1];
	}

	// retrieve last drop action in the stack
	private AlterColumnAction findLastDropColumnAction(TableColumn column){
		if(columnsActions.get(column) == null)
			return null;
		AlterColumnAction[] dropActions = columnsActions.get(column).stream().filter(action -> action instanceof DropColumnAction).toArray(size -> new AlterColumnAction[size]);
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

	private abstract class AlterAction
	{

		public void execute() throws SQLException, ParseException{
			executeAction();
		}

		protected abstract void executeAction() throws SQLException, ParseException;


		protected CreateTableStatement.Def getTableDefinition() throws SQLException, ParseException {
			String createStatement = table.database.assemble("SELECT sql FROM ", table.database.name(), ".sqlite_master WHERE type = 'table' AND name = ?")
			                                       .execute(table.name())
			                                       .mapFirst(Row::getString);

			return Parser.parse(createStatement, CreateTableStatement.Def::parse);
		}

		protected List<CreateTriggerStatement> getTriggersDefinitions() throws ParseException, SQLException {
			List<CreateTriggerStatement> definitions = new LinkedList();
			Iterator<String> iterator = table.triggers().keySet().iterator();
			while (iterator.hasNext()){
				String createStatement = table.database.assemble("SELECT name, sql, tbl_name FROM ", table.database.name(), ".sqlite_master WHERE type = 'trigger' AND tbl_name = ? AND name = ?")
				                                       .execute(table.name, iterator.next())
				                                       .mapFirst(Row::getString);
				definitions.add(Parser.parse(createStatement, CreateTriggerStatement::parse));
			}

			return definitions;
		}

		protected List<CreateViewStatement> getViewsDefinitions() throws SQLException {
			IterableStream<String> createStatements =  table.database.assemble("SELECT sql FROM ", table.database.name(), ".sqlite_master WHERE type = 'view'")
			                                                         .execute()
			                                                         .map(Row::getString);

			return createStatements.map(createStatement -> {
				try {
					return Parser.parse(createStatement, CreateViewStatement::parse);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return null;
			}).filter(createViewStatement -> {
				if(createViewStatement.as instanceof SelectStatement.Simple){
					SelectStatement.Simple select = (SelectStatement.Simple)createViewStatement.as;
					if(select.from.get().alias.get().equals(table.name()))
						return true;
				}
				return false;
			}).toList();
		}
	}

	private abstract class UpdateAction extends AlterAction{

		protected void updateTable(CreateTableStatement.Def tableDefinition) throws SQLException, ParseException {
			CreateTableStatement.Def temporaryTable = new CreateTableStatement.Def();
			temporaryTable.columns = tableDefinition.columns;
			temporaryTable.name = table.name + "_backup";
			temporaryTable.constraints = tableDefinition.constraints;
			temporaryTable.temporary = true;
			temporaryTable.schema = Optional.empty();

			String createTemporary = temporaryTable.toSQL();

			CreateTableStatement.Def definition = getTableDefinition();

			String populateTemporary = "INSERT INTO " + temporaryTable.name + " SELECT " +
				definition.columns.stream().filter(col -> temporaryTable.columns.contains(col)).map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + table.fullName();

			String dropTable = "DROP TABLE " + table.fullName();

			String createTable = tableDefinition.toSQL();

			String populateTable = "INSERT INTO " + tableDefinition.name + " SELECT " +
				temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + temporaryTable.name;

			String dropTemporary = "DROP TABLE " + temporaryTable.name;

			List<CreateTriggerStatement> triggerDefinitions = getTriggersDefinitions();
			table.database.executeTransaction(new String[]{createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary});

			triggerDefinitions.stream().forEach(def -> {
				def.columns.stream().filter(colName -> !tableDefinition.columns.stream().filter(col -> col.name.equals(colName)).findFirst().isPresent()).forEach(col -> def.columns.remove(col));
				try {
					table.database.execute(def.toSQL());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private abstract class AlterColumnAction extends AlterAction
	{
		protected final TableColumn column;

		AlterColumnAction(TableColumn column) {
			super();
			this.column = column;

		}

		public TableColumn column(){
			return column;
		}

		protected ColumnDefinition getColumnDefinition(TableColumn column) throws TokenizeException {
			ColumnDefinition definition = new ColumnDefinition();
			definition.name = column.name();

			TypeDefinition type = new TypeDefinition();
			type.name = column.affinity().name();

			definition.type = Optional.of(type);

			if(column.unique()){
				definition.constraints.add(new ColumnConstraint.Unique());
			}

			if(!column.nullable()){
				definition.constraints.add(new ColumnConstraint.NotNull());
			}

			if(column.check().isPresent()){
				TokenSource source = TokenSource.from(column.check().get());
				ParserContext context = new ParserContext(source);
				definition.constraints.add(ColumnConstraint.Check.parse(context));
			}

			Index pk = column.parentTable().primaryKey();
			if(pk.getColumns().contains(this) && pk.getColumns().size() == 1){
				ColumnConstraint.PrimaryKey constraint = new ColumnConstraint.PrimaryKey();
				constraint.name = Optional.of(pk.getName());
				constraint.autoincrement = false;
				definition.constraints.add(constraint);
			}

			return definition;
		}
	}

	private class AddColumnAction extends AlterColumnAction{

		AddColumnAction(TableColumn column) {
			super(column);
		}

		public void executeAction() throws SQLException, ParseException {
			String query = "ALTER TABLE " +table.fullName() + "  ADD COLUMN " + getColumnDefinition(column);
			table.database.execute(query);
		}
	}

	private abstract class UpdateColumnAction extends AlterColumnAction{

		UpdateColumnAction(TableColumn column) {
			super(column);
		}

		protected void changeColumns(List<ColumnDefinition> columns) throws SQLException, ParseException {
			CreateTableStatement.Def temporaryTable = getTableDefinition();
			List<ColumnDefinition> oldColumns = temporaryTable.columns;
			temporaryTable.temporary = true;
			temporaryTable.name = table.name + "_backup";
			temporaryTable.schema = Optional.empty();
			temporaryTable.columns = columns;

			String createTemporary = temporaryTable.toSQL();

			String populateTemporary = "INSERT INTO " + temporaryTable.name + " SELECT " +
				oldColumns.stream().filter(col -> columns.contains(col)).map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + table.fullName();

			String dropTable = "DROP TABLE " + table.fullName();

			CreateTableStatement.Def newTable = getTableDefinition();
			newTable.columns = columns;
			String createTable = newTable.toSQL();

			String populateTable = "INSERT INTO " + newTable.name + " SELECT " +
				temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + temporaryTable.name;

			String dropTemporary = "DROP TABLE " + temporaryTable.name;

			List<CreateTriggerStatement> triggerDefinitions = getTriggersDefinitions();
			table.database.executeTransaction(new String[]{createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary});

			triggerDefinitions.stream().forEach(def -> {
				def.columns.remove(column.name());
				try {
					table.database.execute(def.toSQL());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private class DropColumnAction extends UpdateColumnAction{

		DropColumnAction(TableColumn column) {
			super(column);
		}

		public void executeAction() throws SQLException, ParseException {
			List<ColumnDefinition> columns = getTableDefinition().columns;
			columns.remove(columns.stream().filter(col -> col.name.equals(column.name())).findFirst().get());

			changeColumns(columns);
		}
	}

	private class ModifyColumnAction extends UpdateColumnAction{

		private final String originalName;

		ModifyColumnAction(TableColumn column, String originalName) {
			super(column);
			this.originalName = originalName;
		}

		public void executeAction() throws ParseException, SQLException {
			List<ColumnDefinition> columns = getTableDefinition().columns;
			ColumnDefinition definition = columns.stream().filter(col -> col.name.equals(column.name())).findFirst().get();
			columns.set(columns.indexOf(definition), getColumnDefinition(column));

			changeColumns(columns);
		}
	}

	private abstract class UpdateConstraintAction extends AlterAction{

		UpdateConstraintAction() {
			super();
		}

		protected void changeColumns(List<ColumnDefinition> columns) throws SQLException, ParseException {
			CreateTableStatement.Def temporaryTable = getTableDefinition();
			List<ColumnDefinition> oldColumns = temporaryTable.columns;
			temporaryTable.temporary = true;
			temporaryTable.name = table.name + "_backup";
			temporaryTable.schema = Optional.empty();
			temporaryTable.columns = columns;

			String createTemporary = temporaryTable.toSQL();

			String populateTemporary = "INSERT INTO " + temporaryTable.name + " SELECT " +
				oldColumns.stream().filter(col -> columns.contains(col)).map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + table.fullName();

			String dropTable = "DROP TABLE " + table.fullName();

			CreateTableStatement.Def newTable = getTableDefinition();
			newTable.columns = columns;
			String createTable = newTable.toSQL();

			String populateTable = "INSERT INTO " + newTable.name + " SELECT " +
				temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
				" FROM " + temporaryTable.name;

			String dropTemporary = "DROP TABLE " + temporaryTable.name;

			List<CreateTriggerStatement> triggerDefinitions = getTriggersDefinitions();
			table.database.executeTransaction(new String[]{createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary});

			triggerDefinitions.stream().forEach(def -> {
				try {
					table.database.execute(def.toSQL());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		}
	}

	private class PrimaryKeyAction extends AlterAction{

		List<TableColumn> columns;

		public PrimaryKeyAction(List<TableColumn> columns){
			super();
			this.columns = columns;
		}

		@Override
		protected void executeAction() throws SQLException, ParseException {
			CreateTableStatement.Def temporaryTable = getTableDefinition();
			temporaryTable.temporary = true;
			temporaryTable.name = table.name + "_backup";
			temporaryTable.schema = Optional.empty();
			List<IndexedColumn> indexedColumns = ((TableConstraint.Index) temporaryTable.constraints.stream().filter(tableConstraint -> tableConstraint instanceof TableConstraint.Index && ((TableConstraint.Index)tableConstraint).type == TableConstraint.Index.Type.PrimaryKey).findFirst().get()).columns;
			indexedColumns.clear();
			for(TableColumn column : columns){
				Expression.ColumnReference ref = new Expression.ColumnReference();
				ref.column = column.name();
				ref.table = Optional.of(temporaryTable.name);
				ref.schema = Optional.empty();
				IndexedColumn indexedColumn = new IndexedColumn();
				indexedColumn.expression = ref;
			}
			String createTemporary = temporaryTable.toSQL();

			String populateTemporary = "INSERT INTO " + temporaryTable.name + " SELECT " +
					tableDefinition.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
					" FROM " + table.fullName();

			String dropTable = "DROP TABLE " + table.fullName();

			CreateTableStatement.Def newTable = getTableDefinition();
			indexedColumns = ((TableConstraint.Index) newTable.constraints.stream().filter(tableConstraint -> tableConstraint instanceof TableConstraint.Index && ((TableConstraint.Index)tableConstraint).type == TableConstraint.Index.Type.PrimaryKey).findFirst().get()).columns;
			indexedColumns.clear();
			for(String column : columns){
				Expression.ColumnReference ref = new Expression.ColumnReference();
				ref.column = column;
				ref.table = Optional.of(newTable.name);
				ref.schema = newTable.schema;
				IndexedColumn indexedColumn = new IndexedColumn();
				indexedColumn.expression = ref;
			}
			String createTable = newTable.toSQL();

			String populateTable = "INSERT INTO " + newTable.name + " SELECT " +
					temporaryTable.columns.stream().map(col -> col.name).collect(Collectors.joining(", ")) +
					" FROM " + temporaryTable.name;

			String dropTemporary = "DROP TABLE " + temporaryTable.name;

			List<CreateTriggerStatement> triggerDefinitions = getTriggersDefinitions();
			table.database.executeTransaction(new String[]{createTemporary, populateTemporary, dropTable, createTable, populateTable, dropTemporary});

			triggerDefinitions.stream().forEach(def -> {
				try {
					table.database.execute(def.toSQL());
				} catch (SQLException e) {
					e.printStackTrace();
				}
			});
		}
	}
}

